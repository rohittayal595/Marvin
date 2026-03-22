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
    val isOnboardingComplete: Boolean
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val STARTING_WEIGHT = floatPreferencesKey("starting_weight")
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                heightCm = preferences[HEIGHT_CM] ?: 0f,
                startingWeight = preferences[STARTING_WEIGHT] ?: 0f,
                targetWeight = preferences[TARGET_WEIGHT] ?: 0f,
                isOnboardingComplete = preferences[IS_ONBOARDING_COMPLETE] ?: false
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
}
