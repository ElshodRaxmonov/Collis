package com.example.collis.presentation.ui.student.home

import com.example.collis.domain.model.AnnouncementModel
import com.example.collis.domain.model.LessonModel
import com.example.collis.domain.model.Task

sealed class HomeUiState {
    /**
     * Initial loading state
     * Shows shimmer placeholders
     */
    data object Loading : HomeUiState()

    /**
     * Successfully loaded all data
     *
     * @param greeting Personalized greeting message
     * @param todayLessons Today's schedule
     * @param upcomingTasks Next 5 tasks due
     * @param recentAnnouncements Latest announcements
     * @param pendingTaskCount Badge count
     * @param currentLesson Currently happening lesson (if any)
     */
    data class Success(
        val greeting: String,
        val todayLessons: List<LessonModel>,
        val upcomingTasks: List<Task>,
        val recentAnnouncements: List<AnnouncementModel>,
        val pendingTaskCount: Int,
        val overdueTaskCount: Int,
        val currentLesson: LessonModel? = null
    ) : HomeUiState()

    /**
     * Error loading data
     * Shows error message with retry
     */
    data class Error(val message: String) : HomeUiState()

    /**
     * Partial success
     * Some data loaded, some failed
     * Still show what we have
     *
     * GOOD UX: Don't block entire screen for one failed request
     */
    data class PartialSuccess(
        val greeting: String,
        val todayLessons: List<LessonModel>?,
        val upcomingTasks: List<Task>?,
        val recentAnnouncements: List<AnnouncementModel>?,
        val errors: List<String>
    ) : HomeUiState()
}