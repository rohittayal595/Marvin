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

data class SettingsState(
    val reductionObese: String = "0.012",
    val reductionOverweight: String = "0.01",
    val reductionNormal: String = "0.08"
)

class SettingsViewModel(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.userPreferencesFlow.collect { prefs ->
                _state.value = SettingsState(
                    reductionObese = prefs.reductionObese.toString(),
                    reductionOverweight = prefs.reductionOverweight.toString(),
                    reductionNormal = prefs.reductionNormal.toString()
                )
            }
        }
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

    fun saveSettings(onComplete: () -> Unit) {
        viewModelScope.launch {
            val obese = _state.value.reductionObese.toFloatOrNull() ?: 0.012f
            val overweight = _state.value.reductionOverweight.toFloatOrNull() ?: 0.01f
            val normal = _state.value.reductionNormal.toFloatOrNull() ?: 0.08f
            
            prefsRepo.saveReductionRates(obese, overweight, normal)
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