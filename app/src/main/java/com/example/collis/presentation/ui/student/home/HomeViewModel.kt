package com.example.collis.presentation.ui.student.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.repository.AuthRepository
import com.example.collis.domain.repository.NotificationRepository
import com.example.collis.domain.repository.TaskRepository
import com.example.collis.domain.usecase.lesson.GetTodayLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject


/**
 * Student Home ViewModel
 *
 * RESPONSIBILITIES:
 * - Load dashboard data (lessons, tasks, announcements)
 * - Handle pull-to-refresh
 * - Provide quick actions
 * - Calculate current lesson status
 *
 * DATA SOURCES:
 * - Lessons: From API (today's schedule)
 * - Tasks: From Room DB (upcoming tasks)
 * - Announcements: From API (recent notifications)
 * - User info: From DataStore (greeting)
 *
 * LEARNING: Dashboard ViewModels coordinate multiple data sources
 * Use combine() to aggregate data
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayLessonsUseCase: GetTodayLessonsUseCase,
    private val taskRepository: TaskRepository,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * UI State
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Loading indicator for pull-to-refresh
     */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Job to manage dashboard data collection
     */
    private var dataJob: Job? = null

    init {
        loadDashboardData()
    }

    /**
     * Event Handler
     */
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refreshData()
            is HomeUiEvent.ToggleTaskCompletion -> toggleTaskCompletion(event.taskId)
            is HomeUiEvent.RetryLoad -> loadDashboardData()
            else -> {
                // Navigation events handled by UI
            }
        }
    }

    /**
     * Load all dashboard data
     *
     * STRATEGY:
     * - Combine multiple data sources reactively
     * - Group flows to manage complexity (standard combine supports up to 5 flows)
     * - Handle Loading and Error states gracefully
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDashboardData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Define individual flows
            val lessonsFlow = getTodayLessonsUseCase(includeCompleted = true)
            val upcomingTasksFlow = taskRepository.getUpcomingTasks(limit = 5)
            val announcementsFlow = notificationRepository.getNotifications()
            val userFlow = authRepository.getCurrentUser()
            
            // Group statistics flows to keep main combine() manageable (max 5 arguments)
            val statsFlow = combine(
                taskRepository.getPendingTaskCount(),
                taskRepository.getOverdueTaskCount()
            ) { pending, overdue -> pending to overdue }

            combine(
                lessonsFlow,
                upcomingTasksFlow,
                announcementsFlow,
                userFlow,
                statsFlow
            ) { lessonsResult, upcomingTasks, announcementsResult, user, stats ->
                val (pendingCount, overdueCount) = stats

                /**
                 * GRACEFUL DEGRADATION & LOADING:
                 * Stay in Loading state if mandatory network sources are still loading
                 */
                if (lessonsResult is NetworkResult.Loading || announcementsResult is NetworkResult.Loading) {
                    return@combine HomeUiState.Loading
                }

                // Extract lessons
                val lessons = when (lessonsResult) {
                    is NetworkResult.Success -> lessonsResult.data ?: emptyList()
                    else -> emptyList()
                }

                // Extract announcements
                val announcements = when (announcementsResult) {
                    is NetworkResult.Success -> announcementsResult.data?.take(3) ?: emptyList()
                    else -> emptyList()
                }

                // Generate greeting
                val greeting = generateGreeting(user?.fullName ?: "Student")

                // Find current lesson (if any)
                val currentLesson = lessons.firstOrNull { it.isLive() }

                /**
                 * Check if any source failed
                 */
                val errors = mutableListOf<String>()
                if (lessonsResult is NetworkResult.Error) {
                    errors.add("Failed to load schedule")
                }
                if (announcementsResult is NetworkResult.Error) {
                    errors.add("Failed to load announcements")
                }

                /**
                 * Create final state
                 */
                if (errors.isEmpty()) {
                    HomeUiState.Success(
                        greeting = greeting,
                        todayLessons = lessons,
                        upcomingTasks = upcomingTasks,
                        recentAnnouncements = announcements,
                        pendingTaskCount = pendingCount,
                        overdueTaskCount = overdueCount,
                        currentLesson = currentLesson
                    )
                } else {
                    HomeUiState.PartialSuccess(
                        greeting = greeting,
                        todayLessons = lessons.takeIf { it.isNotEmpty() },
                        upcomingTasks = upcomingTasks.takeIf { it.isNotEmpty() },
                        recentAnnouncements = announcements.takeIf { it.isNotEmpty() },
                        errors = errors
                    )
                }
            }
                .catch { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to load dashboard"
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * Refresh data (Pull-to-refresh)
     */
    private fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true

            // Trigger remote refresh
            try {
                notificationRepository.refreshNotifications()
            } catch (e: Exception) {
                // Background refresh errors ignored
            }

            // Reload dashboard
            loadDashboardData()
            
            // Wait until we have a non-loading state
            uiState.first { it !is HomeUiState.Loading }

            _isRefreshing.value = false
        }
    }

    /**
     * Toggle task completion
     */
    private fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(taskId)
            } catch (e: Exception) {
                // Error handling (e.g. snackbar) could go here
            }
        }
    }

    /**
     * Generate personalized greeting
     */
    private fun generateGreeting(userName: String): String {
        val hour = LocalTime.now().hour

        val timeGreeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }

        return "$timeGreeting, $userName"
    }
}

/**
 * TESTING EXAMPLE:
 *
 * @Test
 * fun `loadDashboardData with all success shows Success state`() = runTest {
 *     // Given
 *     val mockLessons = listOf(mockLesson1, mockLesson2)
 *     val mockTasks = listOf(mockTask1, mockTask2)
 *
 *     whenever(getTodayLessonsUseCase()).thenReturn(
 *         flowOf(NetworkResult.Success(mockLessons))
 *     )
 *     whenever(taskRepository.getUpcomingTasks()).thenReturn(
 *         flowOf(mockTasks)
 *     )
 *
 *     // When
 *     val viewModel = HomeViewModel(...)
 *     advanceUntilIdle()
 *
 *     // Then
 *     val state = viewModel.uiState.value
 *     assertTrue(state is HomeUiState.Success)
 *     assertEquals(mockLessons, (state as HomeUiState.Success).todayLessons)
 * }
 */
