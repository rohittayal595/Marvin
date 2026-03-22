package com.pulsar.marvin.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OnboardingSettingsState(
    val reductionObese: String = "0.012",
    val reductionOverweight: String = "0.01",
    val reductionNormal: String = "0.008",
    val useFeetAndInches: Boolean = true,
)

class OnboardingSettingsViewModel(
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

  val state: StateFlow<OnboardingSettingsState> = prefsRepo.userPreferencesFlow
    .map { prefs ->
      OnboardingSettingsState(
        reductionObese = prefs.reductionObese.toString(),
        reductionOverweight = prefs.reductionOverweight.toString(),
        reductionNormal = prefs.reductionNormal.toString(),
        useFeetAndInches = prefs.useFeetAndInches
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = OnboardingSettingsState()
    )

  fun saveSettings(
      reductionObese: String,
      reductionOverweight: String,
      reductionNormal: String,
      useFeetAndInches: Boolean,
      onComplete: () -> Unit,
  ) {
    viewModelScope.launch {
      val obese = reductionObese.toFloatOrNull() ?: 0.012f
      val overweight = reductionOverweight.toFloatOrNull() ?: 0.01f
      val normal = reductionNormal.toFloatOrNull() ?: 0.008f

      prefsRepo.saveSettingsData(
        height = 0f,
        targetWeight = 0f,
        obese = obese,
        overweight = overweight,
        normal = normal,
        useFeetAndInches = useFeetAndInches
      )
      onComplete()
    }
  }
}

class OnboardingSettingsViewModelFactory(
    private val prefsRepo: UserPreferencesRepository,
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(OnboardingSettingsViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return OnboardingSettingsViewModel(prefsRepo) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
