package com.pulsar.marvin.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulsar.marvin.data.local.DailyLogDao
import com.pulsar.marvin.data.local.WeeklyPlanDao
import com.pulsar.marvin.data.model.DailyLog
import com.pulsar.marvin.data.model.WeeklyPlan
import com.pulsar.marvin.data.prefs.UserPreferences
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class RoadmapState(
    val isLoading: Boolean = true,
    val userPreferences: UserPreferences? = null,
    val weeklyPlans: List<WeeklyPlan> = emptyList(),
    val dailyLogs: List<DailyLog> = emptyList(),
    val currentWeekStartMillis: Long = 0,
    val showCheckInModal: Boolean = false
)

class RoadmapViewModel(
    private val prefsRepo: UserPreferencesRepository,
    private val weeklyPlanDao: WeeklyPlanDao,
    private val dailyLogDao: DailyLogDao
) : ViewModel() {

    private val _state = MutableStateFlow(RoadmapState())
    val state: StateFlow<RoadmapState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefsRepo.userPreferencesFlow,
                weeklyPlanDao.getAllPlans(),
                dailyLogDao.getAllLogs()
            ) { prefs, plans, logs ->

                val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                val daysToSubtract = today.dayOfWeek.value % 7
                val currentWeekStartMillis = today.minusDays(daysToSubtract.toLong()).toInstant().toEpochMilli() // start of week: Sunday
                RoadmapState(
                    isLoading = false,
                    userPreferences = prefs,
                    weeklyPlans = plans,
                    dailyLogs = logs,
                    currentWeekStartMillis = currentWeekStartMillis,
                    showCheckInModal = _state.value.showCheckInModal
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun setShowCheckInModal(show: Boolean) {
        _state.value = _state.value.copy(showCheckInModal = show)
    }

    fun saveDailyLog(weight: Float?, calories: Int?, dateMillis: Long) {
        viewModelScope.launch {
            dailyLogDao.insert(DailyLog(dateMillis = dateMillis, weight = weight, calories = calories))
            setShowCheckInModal(false)
        }
    }

    fun deleteDailyLog(log: DailyLog) {
        viewModelScope.launch {
            dailyLogDao.delete(log)
        }
    }

    fun recalculatePlans(currentWeek: Int, actualWeight: Float) {
        viewModelScope.launch {
            val prefs = _state.value.userPreferences ?: return@launch
            val targetWeight = prefs.targetWeight
            val height = prefs.heightCm
            val heightM = height / 100f

            if (heightM <= 0) return@launch

            val currentPlan = _state.value.weeklyPlans.find { it.weekNumber == currentWeek }
            val currentWeekStartMillis = currentPlan?.startOfWeekMillis ?: return@launch

            // Delete all plans after currentWeek
            weeklyPlanDao.deletePlansAfter(currentWeek)

            val newPlans = mutableListOf<WeeklyPlan>()
            var currentWeight = if (actualWeight > 0f) actualWeight else (currentPlan.targetWeight)
            var week = currentWeek + 1
            val baseDate = Instant.ofEpochMilli(currentWeekStartMillis).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault())

            while (currentWeight > targetWeight) {
                val bmi = currentWeight / (heightM * heightM)
                val reductionRate = when {
                    bmi >= 30f -> prefs.reductionObese
                    bmi >= 25f -> prefs.reductionOverweight
                    bmi >= 18.5f -> prefs.reductionNormal
                    else -> prefs.reductionNormal
                }
                currentWeight -= (currentWeight * reductionRate)

                val startOfWeekMillis = baseDate.plusWeeks((week - currentWeek).toLong()).toInstant().toEpochMilli()
                newPlans.add(WeeklyPlan(startOfWeekMillis = startOfWeekMillis, weekNumber = week, targetWeight = currentWeight))
                week++

                // safety break
                if (week > 200) break
            }

            if (newPlans.isNotEmpty()) {
                weeklyPlanDao.insertAll(newPlans)
            }
        }
    }
}

class RoadmapViewModelFactory(
    private val prefsRepo: UserPreferencesRepository,
    private val weeklyPlanDao: WeeklyPlanDao,
    private val dailyLogDao: DailyLogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoadmapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoadmapViewModel(prefsRepo, weeklyPlanDao, dailyLogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
