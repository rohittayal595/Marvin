package com.pulsar.marvin.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.pulsar.marvin.data.local.DailyLogDao
import com.pulsar.marvin.data.local.WeeklyPlanDao
import com.pulsar.marvin.data.model.WeeklyPlan
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

data class SettingsState(
    val height: String = "",
    val targetWeight: String = "",
    val reductionObese: String = "0.012",
    val reductionOverweight: String = "0.01",
    val reductionNormal: String = "0.08",
    val useFeetAndInches: Boolean = true
)

class SettingsViewModel(
    private val prefsRepo: UserPreferencesRepository,
    private val weeklyPlanDao: WeeklyPlanDao,
    private val dailyLogDao: DailyLogDao
) : ViewModel() {

    val state: StateFlow<SettingsState> = prefsRepo.userPreferencesFlow
        .map { prefs ->
            SettingsState(
                height = if (prefs.heightCm > 0f) prefs.heightCm.toString() else "",
                targetWeight = if (prefs.targetWeight > 0f) prefs.targetWeight.toString() else "",
                reductionObese = prefs.reductionObese.toString(),
                reductionOverweight = prefs.reductionOverweight.toString(),
                reductionNormal = prefs.reductionNormal.toString(),
                useFeetAndInches = prefs.useFeetAndInches
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )



    fun saveSettings(
        height: String,
        targetWeight: String,
        reductionObese: String,
        reductionOverweight: String,
        reductionNormal: String,
        useFeetAndInches: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val h = height.toFloatOrNull() ?: 0f
            val tw = targetWeight.toFloatOrNull() ?: 0f
            val obese = reductionObese.toFloatOrNull() ?: 0.012f
            val overweight = reductionOverweight.toFloatOrNull() ?: 0.01f
            val normal = reductionNormal.toFloatOrNull() ?: 0.08f
            
            prefsRepo.saveSettingsData(h, tw, obese, overweight, normal, useFeetAndInches)
            
            recalculatePlans(h, tw, obese, overweight, normal)
            onComplete()
        }
    }

    private suspend fun recalculatePlans(
        height: Float, targetWeight: Float,
        reductionObese: Float, reductionOverweight: Float, reductionNormal: Float
    ) {
        val heightM = height / 100f
        if (heightM <= 0) return

        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        val daysToSubtract = today.dayOfWeek.value % 7
        val currentWeekStartMillis = today.minusDays(daysToSubtract.toLong()).toInstant().toEpochMilli()

        val currentPlan = weeklyPlanDao.getPlanForDate(currentWeekStartMillis)
        val currentWeek = currentPlan?.weekNumber ?: return

        // Delete all plans after currentWeek
        weeklyPlanDao.deletePlansAfter(currentWeek)

        val latestLog = dailyLogDao.getLatestLogWithWeight()
        val actualWeight = latestLog?.weight ?: prefsRepo.userPreferencesFlow.first().startingWeight

        val newPlans = mutableListOf<WeeklyPlan>()
        var currentWeight = if (actualWeight > 0f) actualWeight else (currentPlan.targetWeight)
        var week = currentWeek + 1
        val baseDate = Instant.ofEpochMilli(currentWeekStartMillis).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault())

        while (currentWeight > targetWeight) {
            val bmi = currentWeight / (heightM * heightM)
            val reductionRate = when {
                bmi >= 30f -> reductionObese
                bmi >= 25f -> reductionOverweight
                else -> reductionNormal
            }
            currentWeight -= (currentWeight * reductionRate)

            val startOfWeekMillis = baseDate.plusWeeks((week - currentWeek).toLong()).toInstant().toEpochMilli()
            newPlans.add(WeeklyPlan(startOfWeekMillis = startOfWeekMillis, weekNumber = week, targetWeight = currentWeight))
            week++

            // safety break
            if (week >= 200) break
        }

        if (newPlans.isNotEmpty()) {
            weeklyPlanDao.insertAll(newPlans)
        }
    }
}

class SettingsViewModelFactory(
    private val prefsRepo: UserPreferencesRepository,
    private val weeklyPlanDao: WeeklyPlanDao,
    private val dailyLogDao: DailyLogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(prefsRepo, weeklyPlanDao, dailyLogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}