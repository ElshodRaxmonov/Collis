package com.example.collis.presentation.ui.student.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.AnnouncementModel
import com.example.collis.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedFilter = MutableStateFlow(NotificationFilter.ALL)
    val selectedFilter: StateFlow<NotificationFilter> = _selectedFilter.asStateFlow()

    private var allNotifications: List<AnnouncementModel> = emptyList()

    init {
        refresh()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getNotifications().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        if (_uiState.value !is NotificationUiState.Success) {
                            _uiState.value = NotificationUiState.Loading
                        }
                    }
                    is NetworkResult.Success -> {
                        allNotifications = result.data ?: emptyList()
                        
                        // CLEAR BADGE: Update last viewed ID when notifications are loaded
                        if (allNotifications.isNotEmpty()) {
                            preferencesManager.saveLastViewedNotificationId(allNotifications.maxOf { it.id })
                        }
                        
                        applyFilter()
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = NotificationUiState.Error(
                            result.message ?: "Failed to load notifications"
                        )
                    }
                }
            }
        }
    }

    fun setFilter(filter: NotificationFilter) {
        _selectedFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (_selectedFilter.value) {
            NotificationFilter.ALL -> allNotifications
            NotificationFilter.CANCELLATION -> allNotifications.filter { it.messageType == "CANCELLATION" }
            NotificationFilter.RESCHEDULE -> allNotifications.filter { it.messageType == "RESCHEDULE" }
            NotificationFilter.INFO -> allNotifications.filter {
                it.messageType != "CANCELLATION" && it.messageType != "RESCHEDULE"
            }
        }
        _uiState.value = NotificationUiState.Success(filtered)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                notificationRepository.refreshNotifications()
            } catch (_: Exception) { }
            
            loadNotifications()
            _isRefreshing.value = false
        }
    }
}
