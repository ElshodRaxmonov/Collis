package com.example.collis.presentation.ui.student.task

import com.example.collis.domain.model.Task


/**
 * Task Manager UI State
 *
 * FEATURES:
 * - List all tasks
 * - Filter (All, Pending, Completed, Overdue)
 * - Sort (Due date, Priority, Created date)
 * - Search by title/description
 * - Bulk actions (delete completed)
 *
 * LEARNING: Complex list screens need rich state
 * Filter + Sort + Search = Better UX
 */
sealed class TaskManagerUiState {
    /**
     * Loading tasks
     */
    data object Loading : TaskManagerUiState()

    /**
     * Successfully loaded tasks
     *
     * @param allTasks Original list (before filters)
     * @param displayedTasks Filtered & sorted list to show
     * @param selectedFilter Currently active filter
     * @param selectedSort Currently active sort
     * @param searchQuery Current search text
     * @param pendingCount Count for filter badges
     * @param completedCount Count for filter badges
     * @param overdueCount Count for filter badges
     */
    data class Success(
        val allTasks: List<Task>,
        val displayedTasks: List<Task>,
        val selectedFilter: TaskFilter,
        val selectedSort: TaskSort,
        val searchQuery: String,
        val pendingCount: Int,
        val completedCount: Int,
        val overdueCount: Int
    ) : TaskManagerUiState()

    /**
     * Error loading tasks
     */
    data class Error(val message: String) : TaskManagerUiState()

    /**
     * Empty state (no tasks at all)
     */
    data object Empty : TaskManagerUiState()
}

/**
 * Task Filter Options
 */
enum class TaskFilter(val displayName: String) {
    ALL("All Tasks"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    OVERDUE("Overdue"),
    TODAY("Due Today"),
    THIS_WEEK("This Week")
}

/**
 * Task Sort Options
 */
enum class TaskSort(val displayName: String) {
    DUE_DATE_ASC("Due Date (Soon First)"),
    DUE_DATE_DESC("Due Date (Later First)"),
    PRIORITY_HIGH_TO_LOW("Priority (High to Low)"),
    PRIORITY_LOW_TO_HIGH("Priority (Low to High)"),
    CREATED_NEW_FIRST("Created (Newest)"),
    CREATED_OLD_FIRST("Created (Oldest)"),
    ALPHABETICAL("Alphabetical")
}

