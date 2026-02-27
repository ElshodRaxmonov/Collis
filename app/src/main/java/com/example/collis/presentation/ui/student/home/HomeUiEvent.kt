package com.example.collis.presentation.ui.student.home

/**
 * Student Home UI Events
 * User interactions on home screen
 */
sealed class HomeUiEvent {
    /**
     * Refresh all data
     * Pull-to-refresh gesture
     */
    data object Refresh : HomeUiEvent()

    /**
     * Navigate to lesson detail
     */
    data class NavigateToLesson(val lessonId: Int) : HomeUiEvent()

    /**
     * Navigate to task detail
     */
    data class NavigateToTask(val taskId: Long) : HomeUiEvent()

    /**
     * Navigate to announcement detail
     */
    data class NavigateToAnnouncement(val announcementId: Int) : HomeUiEvent()

    /**
     * Navigate to full schedule
     */
    data object NavigateToSchedule : HomeUiEvent()

    /**
     * Navigate to all tasks
     */
    data object NavigateToTasks : HomeUiEvent()

    /**
     * Navigate to all announcements
     */
    data object NavigateToAnnouncements : HomeUiEvent()

    /**
     * Quick add task from FAB
     */
    data object NavigateToAddTask : HomeUiEvent()

    /**
     * Toggle task completion from home
     */
    data class ToggleTaskCompletion(val taskId: Long) : HomeUiEvent()

    /**
     * Retry loading failed data
     */
    data object RetryLoad : HomeUiEvent()
}