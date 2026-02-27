package com.example.collis.presentation.ui.student.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.local.CollisDatabase
import com.example.collis.data.local.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val database: CollisDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Combine user info flows
            val userInfoFlow = combine(
                preferencesManager.fullName,
                preferencesManager.email,
                preferencesManager.username,
                preferencesManager.groupName,
                preferencesManager.userType
            ) { fullName, email, username, groupName, userType ->
                Triple(
                    Triple(fullName.orEmpty(), email.orEmpty(), username.orEmpty()),
                    groupName.orEmpty(),
                    userType.orEmpty()
                )
            }

            // Combine settings flows
            val settingsFlow = combine(
                preferencesManager.isDarkMode,
                preferencesManager.notificationsEnabled
            ) { darkMode, notifications -> Pair(darkMode, notifications) }

            // Combine both
            combine(userInfoFlow, settingsFlow) { userInfo, settings ->
                ProfileUiState(
                    fullName = userInfo.first.first,
                    email = userInfo.first.second,
                    username = userInfo.first.third,
                    groupName = userInfo.second,
                    userType = userInfo.third,
                    isDarkMode = settings.first,
                    notificationsEnabled = settings.second,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.toggleDarkMode(enabled)
            _isDark.value = enabled
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearSession()
            database.clearAllTables()
            _logoutEvent.emit(Unit)
        }
    }
}
