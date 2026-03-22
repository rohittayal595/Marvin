package com.pulsar.marvin.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val heightCm: Float,
    val startingWeight: Float,
    val targetWeight: Float,
    val isOnboardingComplete: Boolean,
    val reductionObese: Float,
    val reductionOverweight: Float,
    val reductionNormal: Float
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val STARTING_WEIGHT = floatPreferencesKey("starting_weight")
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val REDUCTION_OBESE = floatPreferencesKey("reduction_obese")
        val REDUCTION_OVERWEIGHT = floatPreferencesKey("reduction_overweight")
        val REDUCTION_NORMAL = floatPreferencesKey("reduction_normal")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                heightCm = preferences[HEIGHT_CM] ?: 0f,
                startingWeight = preferences[STARTING_WEIGHT] ?: 0f,
                targetWeight = preferences[TARGET_WEIGHT] ?: 0f,
                isOnboardingComplete = preferences[IS_ONBOARDING_COMPLETE] ?: false,
                reductionObese = preferences[REDUCTION_OBESE] ?: 0.012f,
                reductionOverweight = preferences[REDUCTION_OVERWEIGHT] ?: 0.01f,
                reductionNormal = preferences[REDUCTION_NORMAL] ?: 0.08f
            )
        }

    suspend fun saveOnboardingData(height: Float, startingWeight: Float, targetWeight: Float) {
        dataStore.edit { preferences ->
            preferences[HEIGHT_CM] = height
            preferences[STARTING_WEIGHT] = startingWeight
            preferences[TARGET_WEIGHT] = targetWeight
            preferences[IS_ONBOARDING_COMPLETE] = true
        }
    }
    
    suspend fun saveReductionRates(obese: Float, overweight: Float, normal: Float) {
        dataStore.edit { preferences ->
            preferences[REDUCTION_OBESE] = obese
            preferences[REDUCTION_OVERWEIGHT] = overweight
            preferences[REDUCTION_NORMAL] = normal
        }
    }
    
    suspend fun saveSettingsData(height: Float, targetWeight: Float, obese: Float, overweight: Float, normal: Float) {
        dataStore.edit { preferences ->
            if (height > 0f) preferences[HEIGHT_CM] = height
            if (targetWeight > 0f) preferences[TARGET_WEIGHT] = targetWeight
            preferences[REDUCTION_OBESE] = obese
            preferences[REDUCTION_OVERWEIGHT] = overweight
            preferences[REDUCTION_NORMAL] = normal
        }
    }
}