package com.example.collis.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.domain.repository.NotificationRepository
import com.example.collis.data.network.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * MainViewModel — provides app-level state.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    /** Whether the user has selected dark mode. */
    val isDarkMode: StateFlow<Boolean> = preferencesManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /** 
     * Count of new notifications since last seen.
     * Compares server IDs against local 'lastViewedNotificationId'.
     */
    val newNotificationCount: StateFlow<Int> = combine(
        flow {
            while (true) {
                if (preferencesManager.getAccessToken() != null) {
                    val result = notificationRepository.getNotifications().first { it !is NetworkResult.Loading }
                    if (result is NetworkResult.Success) {
                        emit(result.data ?: emptyList())
                    }
                } else {
                    emit(emptyList())
                }
                delay(60_000) // Poll every 60s
            }
        },
        preferencesManager.lastViewedNotificationIdFlow
    ) { notifications, lastViewedId ->
        // Only count items with an ID greater than the last one the user actually viewed
        notifications.count { it.id > lastViewedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )
}
