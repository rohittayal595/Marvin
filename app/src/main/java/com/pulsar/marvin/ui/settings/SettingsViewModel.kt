package com.pulsar.marvin.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulsar.marvin.data.prefs.UserPreferences
import com.pulsar.marvin.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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