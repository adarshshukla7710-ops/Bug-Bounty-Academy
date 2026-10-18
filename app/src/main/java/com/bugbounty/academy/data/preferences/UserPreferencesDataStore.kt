package com.bugbounty.academy.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val USER_XP = intPreferencesKey("user_xp")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_ACTIVE_TIMESTAMP = longPreferencesKey("last_active_timestamp")
        val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, DARK, LIGHT
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val COMPLETED_INITIAL_ONBOARDING = booleanPreferencesKey("completed_initial_onboarding")
    }

    val userXp: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[PreferencesKeys.USER_XP] ?: 0 }

    val currentStreak: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[PreferencesKeys.CURRENT_STREAK] ?: 1 }

    val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[PreferencesKeys.THEME_MODE] ?: "DARK" }

    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true }

    suspend fun addXp(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.USER_XP] ?: 0
            preferences[PreferencesKeys.USER_XP] = current + amount
        }
    }

    suspend fun recordActivityToday() {
        val now = System.currentTimeMillis()
        context.dataStore.edit { preferences ->
            val lastActive = preferences[PreferencesKeys.LAST_ACTIVE_TIMESTAMP] ?: 0L
            val currentStreak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0

            if (lastActive == 0L) {
                preferences[PreferencesKeys.CURRENT_STREAK] = 1
            } else {
                val diffMillis = now - lastActive
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

                if (diffDays == 1L) {
                    preferences[PreferencesKeys.CURRENT_STREAK] = currentStreak + 1
                } else if (diffDays > 1L) {
                    preferences[PreferencesKeys.CURRENT_STREAK] = 1
                }
            }
            preferences[PreferencesKeys.LAST_ACTIVE_TIMESTAMP] = now
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
