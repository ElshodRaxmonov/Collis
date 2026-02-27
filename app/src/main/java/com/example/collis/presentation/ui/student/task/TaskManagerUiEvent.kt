package com.example.collis.presentation.ui.student.task

import com.example.collis.domain.model.Task

/**
 * Task Manager UI Events
 */
sealed class TaskManagerUiEvent {
    /**
     * Filter tasks
     */
    data class ChangeFilter(val filter: TaskFilter) : TaskManagerUiEvent()

    /**
     * Sort tasks
     */
    data class ChangeSort(val sort: TaskSort) : TaskManagerUiEvent()

    /**
     * Search tasks
     */
    data class SearchTasks(val query: String) : TaskManagerUiEvent()

    /**
     * Clear search
     */
    data object ClearSearch : TaskManagerUiEvent()

    /**
     * Navigate to add task
     */
    data object NavigateToAddTask : TaskManagerUiEvent()

    /**
     * Navigate to edit task
     */
    data class NavigateToEditTask(val taskId: Long) : TaskManagerUiEvent()

    /**
     * Navigate to task detail
     */
    data class NavigateToTaskDetail(val taskId: Long) : TaskManagerUiEvent()

    /**
     * Toggle task completion
     */
    data class ToggleTaskCompletion(val taskId: Long) : TaskManagerUiEvent()

    /**
     * Delete single task
     */
    data class DeleteTask(val task: Task) : TaskManagerUiEvent()

    /**
     * Undo delete
     */
    data class UndoDelete(val task: Task) : TaskManagerUiEvent()

    /**
     * Delete all completed tasks
     */
    data object DeleteAllCompleted : TaskManagerUiEvent()

    /**
     * Refresh tasks
     */
    data object Refresh : TaskManagerUiEvent()

    /**
     * Show filter bottom sheet
     */
    data object ShowFilterSheet : TaskManagerUiEvent()

    /**
     * Show sort bottom sheet
     */
    data object ShowSortSheet : TaskManagerUiEvent()
}