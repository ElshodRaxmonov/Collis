package com.example.collis.presentation.ui.student.notification

import com.example.collis.domain.model.AnnouncementModel

sealed class NotificationUiState {
    data object Loading : NotificationUiState()
    data class Success(val notifications: List<AnnouncementModel>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

enum class NotificationFilter(val displayName: String) {
    ALL("All"),
    CANCELLATION("Cancelled"),
    RESCHEDULE("Rescheduled"),
    INFO("Info")
}
