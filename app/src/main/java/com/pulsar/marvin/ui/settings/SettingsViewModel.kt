package com.pulsar.marvin.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulsar.marvin.data.prefs.UserPreferences
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = prefsRepo.userPreferencesFlow.first()
            _state.value = SettingsState(
                height = if (prefs.heightCm > 0f) prefs.heightCm.toString() else "",
                targetWeight = if (prefs.targetWeight > 0f) prefs.targetWeight.toString() else "",
                reductionObese = prefs.reductionObese.toString(),
                reductionOverweight = prefs.reductionOverweight.toString(),
                reductionNormal = prefs.reductionNormal.toString(),
                useFeetAndInches = prefs.useFeetAndInches
            )
        }
    }

    fun updateHeight(value: String) {
        _state.value = _state.value.copy(height = value)
    }

    fun updateTargetWeight(value: String) {
        _state.value = _state.value.copy(targetWeight = value)
    }

    fun updateReductionObese(value: String) {
        _state.value = _state.value.copy(reductionObese = value)
    }

    fun updateReductionOverweight(value: String) {
        _state.value = _state.value.copy(reductionOverweight = value)
    }

    fun updateReductionNormal(value: String) {
        _state.value = _state.value.copy(reductionNormal = value)
    }

    fun updateUseFeetAndInches(value: Boolean) {
        _state.value = _state.value.copy(useFeetAndInches = value)
    }

    fun saveSettings(onComplete: () -> Unit) {
        viewModelScope.launch {
            val height = _state.value.height.toFloatOrNull() ?: 0f
            val targetWeight = _state.value.targetWeight.toFloatOrNull() ?: 0f
            val obese = _state.value.reductionObese.toFloatOrNull() ?: 0.012f
            val overweight = _state.value.reductionOverweight.toFloatOrNull() ?: 0.01f
            val normal = _state.value.reductionNormal.toFloatOrNull() ?: 0.08f
            val useFeetAndInches = _state.value.useFeetAndInches
            
            prefsRepo.saveSettingsData(height, targetWeight, obese, overweight, normal, useFeetAndInches)
            onComplete()
        }
    }
}

class SettingsViewModelFactory(
    private val prefsRepo: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(prefsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}