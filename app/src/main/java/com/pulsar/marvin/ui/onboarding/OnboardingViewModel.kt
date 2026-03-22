package com.pulsar.marvin.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulsar.marvin.data.local.WeeklyPlanDao
import com.pulsar.marvin.data.model.WeeklyPlan
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val REDUCTION_OBESE = 0.012f
const val REDUCTION_OVERWEIGHT = 0.01f
const val REDUCTION_NORMAL = 0.01f // TODO: Maybe reduce to 0.08f

class OnboardingViewModel(
  private val prefsRepo: UserPreferencesRepository,
  private val weeklyPlanDao: WeeklyPlanDao,
) : ViewModel() {

  private val _heightText = MutableStateFlow("")
  val heightText: StateFlow<String> = _heightText.asStateFlow()

  private val _weightText = MutableStateFlow("")
  val weightText: StateFlow<String> = _weightText.asStateFlow()

  private val _targetWeightText = MutableStateFlow("")
  val targetWeightText: StateFlow<String> = _targetWeightText.asStateFlow()

  fun updateHeight(value: String) {
    _heightText.value = value
  }

  fun updateWeight(value: String) {
    _weightText.value = value
  }

  fun updateTargetWeight(value: String) {
    _targetWeightText.value = value
  }

  fun getHealthyRange(heightStr: String): Pair<Float, Float> {
    val heightCm = heightStr.replace(",", ".").toFloatOrNull() ?: return Pair(0f, 0f)
    val heightM = heightCm / 100f
    if (heightM <= 0) return Pair(0f, 0f)
    val minWeight = 18.5f * heightM * heightM
    val maxWeight = 24.9f * heightM * heightM
    return Pair(minWeight, maxWeight)
  }

  fun completeOnboarding(onComplete: () -> Unit) {
    val height = _heightText.value.replace(",", ".").toFloatOrNull() ?: return
    val weight = _weightText.value.replace(",", ".").toFloatOrNull() ?: return
    val targetWeight = _targetWeightText.value.replace(",", ".").toFloatOrNull() ?: return

    viewModelScope.launch {
      try {
        // Save preferences
        prefsRepo.saveOnboardingData(height, weight, targetWeight)

        // Generate plan
        val plans = mutableListOf<WeeklyPlan>()

        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        val daysToSubtract = today.dayOfWeek.value % 7
        val baseDate = today.minusDays(daysToSubtract.toLong()) // start of week: Sunday

        var currentWeight = weight
        var week = 1
        val heightM = height / 100f

        if (heightM > 0) {
          while (currentWeight > targetWeight) {
            val bmi = currentWeight / (heightM * heightM)
            val reductionRate = when {
              bmi >= 30f -> REDUCTION_OBESE
              bmi >= 25f -> REDUCTION_OVERWEIGHT
              bmi >= 18.5f -> REDUCTION_NORMAL
              else -> REDUCTION_NORMAL
            }
            currentWeight -= (currentWeight * reductionRate)
            // if (currentWeight < targetWeight) {
            //     currentWeight = targetWeight
            // }

            val startOfWeekMillis = baseDate.plusWeeks(week.toLong() - 1L).toInstant().toEpochMilli()
            plans.add(WeeklyPlan(startOfWeekMillis = startOfWeekMillis, weekNumber = week, targetWeight = currentWeight))
            week++

            // safety break
            if (week > 200) break
          }
        }

        // Make sure there is at least one plan
        if (plans.isEmpty()) {
          val startOfWeekMillis = baseDate.toInstant().toEpochMilli()
          plans.add(WeeklyPlan(startOfWeekMillis = startOfWeekMillis, weekNumber = 1, targetWeight = targetWeight))
        }

        weeklyPlanDao.deleteAll()
        weeklyPlanDao.insertAll(plans)

        onComplete()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}

class OnboardingViewModelFactory(
  private val prefsRepo: UserPreferencesRepository,
  private val weeklyPlanDao: WeeklyPlanDao,
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return OnboardingViewModel(prefsRepo, weeklyPlanDao) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
