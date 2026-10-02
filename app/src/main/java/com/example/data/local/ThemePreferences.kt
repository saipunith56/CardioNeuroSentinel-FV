package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.themeDataStore)

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
        val PRIVACY_EPSILON_KEY = floatPreferencesKey("differential_privacy_epsilon")
        const val DEFAULT_DARK_THEME = false
        const val DEFAULT_PRIVACY_EPSILON = 0.50f

        @Volatile
        private var INSTANCE: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val isDarkThemeFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DARK_THEME_KEY] ?: DEFAULT_DARK_THEME
        }

    val privacyEpsilonFlow: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PRIVACY_EPSILON_KEY] ?: DEFAULT_PRIVACY_EPSILON
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun setPrivacyEpsilon(epsilon: Float) {
        dataStore.edit { preferences ->
            preferences[PRIVACY_EPSILON_KEY] = epsilon
        }
    }

    suspend fun getInitialDarkTheme(): Boolean {
        return try {
            dataStore.data.first()[DARK_THEME_KEY] ?: DEFAULT_DARK_THEME
        } catch (e: Exception) {
            DEFAULT_DARK_THEME
        }
    }

    suspend fun getInitialPrivacyEpsilon(): Float {
        return try {
            dataStore.data.first()[PRIVACY_EPSILON_KEY] ?: DEFAULT_PRIVACY_EPSILON
        } catch (e: Exception) {
            DEFAULT_PRIVACY_EPSILON
        }
    }
}
