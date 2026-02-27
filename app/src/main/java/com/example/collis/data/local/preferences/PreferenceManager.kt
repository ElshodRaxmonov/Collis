package com.example.collis.data.local.preferences


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "collis_preferences"
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // JWT Tokens
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        // User Info
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_TYPE = stringPreferencesKey("user_type") // "ST" or "LC"
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_GROUP_NAME = stringPreferencesKey("group_name")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

        // App Settings
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Sync Info
        private val KEY_LAST_ALERTED_NOTIFICATION_ID = intPreferencesKey("last_notification_id")
        private val KEY_LAST_VIEWED_NOTIFICATION_ID = intPreferencesKey("last_viewed_notification_id")
    }

    // ==================== TOKEN FLOWS ====================

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_REFRESH_TOKEN]
    }

    // ==================== USER INFO FLOWS ====================

    val userType: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_TYPE]
    }

    val isStudent: Flow<Boolean> = userType.map { it == "STUDENT" }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val fullName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FULL_NAME]
    }

    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_EMAIL]
    }

    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USERNAME]
    }

    val groupName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_GROUP_NAME]
    }

    // ==================== APP SETTINGS & SYNC FLOWS ====================

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: false
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val lastViewedNotificationIdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_VIEWED_NOTIFICATION_ID] ?: 0
    }

    // ==================== SAVE METHODS ====================

    suspend fun saveAuthTokens(
        accessToken: String,
        refreshToken: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) {
                preferences[KEY_REFRESH_TOKEN] = refreshToken
            }
        }
    }

    suspend fun saveUserSession(
        accessToken: String,
        refreshToken: String? = null,
        userId: String,
        userType: String,
        username: String,
        fullName: String,
        email: String,
        groupName: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) {
                preferences[KEY_REFRESH_TOKEN] = refreshToken
            }
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_TYPE] = userType
            preferences[KEY_USERNAME] = username
            preferences[KEY_FULL_NAME] = fullName
            preferences[KEY_EMAIL] = email
            if (groupName != null) {
                preferences[KEY_GROUP_NAME] = groupName
            }
            preferences[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun updateProfile(fullName: String? = null, email: String? = null) {
        context.dataStore.edit { preferences ->
            fullName?.let { preferences[KEY_FULL_NAME] = it }
            email?.let { preferences[KEY_EMAIL] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_TYPE)
            preferences.remove(KEY_USERNAME)
            preferences.remove(KEY_FULL_NAME)
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_GROUP_NAME)
            preferences[KEY_IS_LOGGED_IN] = false
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun toggleDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveLastAlertedNotificationId(id: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_LAST_ALERTED_NOTIFICATION_ID] ?: 0
            if (id > current) {
                preferences[KEY_LAST_ALERTED_NOTIFICATION_ID] = id
            }
        }
    }
    
    suspend fun saveLastViewedNotificationId(id: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_LAST_VIEWED_NOTIFICATION_ID] ?: 0
            if (id > current) {
                preferences[KEY_LAST_VIEWED_NOTIFICATION_ID] = id
            }
        }
    }

    // ==================== UTILITY GETTERS ====================

    suspend fun getAccessToken(): String? {
        return authToken.first()
    }

    suspend fun getRefreshToken(): String? {
        return refreshToken.first()
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[KEY_USER_ID]
    }

    suspend fun getUserType(): String? {
        return userType.first()
    }

    suspend fun getLastAlertedNotificationId(): Int {
        return context.dataStore.data.first()[KEY_LAST_ALERTED_NOTIFICATION_ID] ?: 0
    }
}