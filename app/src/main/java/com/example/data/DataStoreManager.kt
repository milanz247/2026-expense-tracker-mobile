package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_CURRENCY_KEY = stringPreferencesKey("user_currency")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val ONBOARDING_SEEN_KEY = booleanPreferencesKey("onboarding_seen")
    }

    /** Local-only UI flag — whether the first-run Onboarding carousel has been shown once. */
    val onboardingSeenFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_SEEN_KEY] ?: false
    }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_SEEN_KEY] = true
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]?.let { saved ->
            runCatching { ThemeMode.valueOf(saved) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    val nameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val emailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val currencyFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_CURRENCY_KEY]
    }

    suspend fun saveAuthData(token: String, name: String, email: String, currency: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_CURRENCY_KEY] = currency
        }
    }

    /** Updates cached profile fields without touching the JWT — unlike [saveAuthData], never logs the user out. */
    suspend fun updateUserInfo(name: String, email: String, currency: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_CURRENCY_KEY] = currency
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_CURRENCY_KEY)
        }
    }
}
