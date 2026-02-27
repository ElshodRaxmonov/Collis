package com.example.collis.presentation.ui.student.task

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.domain.repository.TaskRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Task Manager ViewModel
 * 
 * RESPONSIBILITIES:
 * - Load all tasks from Room DB
 * - Apply filters (pending, completed, etc.)
 * - Apply sorting (due date, priority, etc.)
 * - Handle search
 * - Manage CRUD operations
 * - Calculate counts for filter badges
 * 
 * STATE MANAGEMENT:
 * - Tasks come from Room via Flow (reactive)
 * - Filter/Sort/Search are local state
 * - Combine all to create displayed list
 * 
 * LEARNING: Complex filtering with reactive data
 * Room emits new data → ViewModel filters → UI updates
 * All automatic!
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TaskManagerViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    /**
     * UI State
     */
    private val _uiState = MutableStateFlow<TaskManagerUiState>(TaskManagerUiState.Loading)
    val uiState: StateFlow<TaskManagerUiState> = _uiState.asStateFlow()

    /**
     * Filter/Sort/Search State
     */
    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(TaskSort.DUE_DATE_ASC)
    val selectedSort: StateFlow<TaskSort> = _selectedSort.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Bottom sheet states
     */
    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _showSortSheet = MutableStateFlow(false)
    val showSortSheet: StateFlow<Boolean> = _showSortSheet.asStateFlow()

    init {
        loadTasks()
    }

    /**
     * Event Handler
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: TaskManagerUiEvent) {
        when (event) {
            is TaskManagerUiEvent.ChangeFilter -> changeFilter(event.filter)
            is TaskManagerUiEvent.ChangeSort -> changeSort(event.sort)
            is TaskManagerUiEvent.SearchTasks -> search(event.query)
            is TaskManagerUiEvent.ClearSearch -> clearSearch()
            is TaskManagerUiEvent.ToggleTaskCompletion -> toggleCompletion(event.taskId)
            is TaskManagerUiEvent.DeleteTask -> deleteTask(event.task)
            is TaskManagerUiEvent.UndoDelete -> undoDelete(event.task)
            is TaskManagerUiEvent.DeleteAllCompleted -> deleteAllCompleted()
            is TaskManagerUiEvent.ShowFilterSheet -> _showFilterSheet.value = true
            is TaskManagerUiEvent.ShowSortSheet -> _showSortSheet.value = true
            is TaskManagerUiEvent.Refresh -> loadTasks()
            else -> {
                // Navigation events handled by UI
            }
        }
    }

    /**
     * Load tasks with reactive filtering
     * 
     * REACTIVE PIPELINE:
     * 1. taskRepository.getAllTasks() - Flow from Room
     * 2. combine with filter, sort, search - MutableStateFlows
     * 3. Apply transformations
     * 4. Emit new UI state
     * 
     * AUTOMATIC UPDATES:
     * - Add task in DB → Flow emits → UI updates
     * - Change filter → combine emits → UI updates
     * - Toggle completion → Flow emits → UI updates
     * 
     * LEARNING: This is the power of reactive programming!
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTasks() {
        viewModelScope.launch {
            /**
             * Combine multiple flows
             * 
             * Note: combine with 6+ flows uses an array for arguments.
             */
            combine(
                taskRepository.getAllTasks(),
                _selectedFilter,
                _selectedSort,
                _searchQuery,
                taskRepository.getPendingTaskCount(),
                taskRepository.getOverdueTaskCount()
            ) { array ->
                val tasks = array[0] as List<Task>
                val filter = array[1] as TaskFilter
                val sort = array[2] as TaskSort
                val search = array[3] as String
                val pendingCount = array[4] as Int
                val overdueCount = array[5] as Int

                /**
                 * Early return for empty state
                 */
                if (tasks.isEmpty() && search.isBlank() && filter == TaskFilter.ALL) {
                    return@combine TaskManagerUiState.Empty
                }

                /**
                 * Apply search filter
                 */
                val searchedTasks = if (search.isNotBlank()) {
                    tasks.filter { task ->
                        task.title.contains(search, ignoreCase = true) ||
                                task.description?.contains(search, ignoreCase = true) == true
                    }
                } else {
                    tasks
                }

                /**
                 * Apply status filter
                 */
                val filteredTasks = when (filter) {
                    TaskFilter.ALL -> searchedTasks
                    TaskFilter.PENDING -> searchedTasks.filter { !it.isCompleted }
                    TaskFilter.COMPLETED -> searchedTasks.filter { it.isCompleted }
                    TaskFilter.OVERDUE -> searchedTasks.filter { it.isOverdue() }
                    TaskFilter.TODAY -> searchedTasks.filter { it.isDueToday() }
                    TaskFilter.THIS_WEEK -> searchedTasks.filter { task ->
                        task.dueDate?.let { dueDate ->
                            val now = LocalDateTime.now()
                            val endOfWeek = now.plusDays(7)
                            dueDate.isAfter(now) && dueDate.isBefore(endOfWeek)
                        } ?: false
                    }
                }

                /**
                 * Apply sorting
                 */
                val sortedTasks = when (sort) {
                    TaskSort.DUE_DATE_ASC -> filteredTasks.sortedWith(
                        compareBy(
                            nullsLast(),
                            { it.dueDate }
                        )
                    )
                    TaskSort.DUE_DATE_DESC -> filteredTasks.sortedWith(
                        compareByDescending(
                            nullsLast(),
                            { it.dueDate }
                        )
                    )
                    TaskSort.PRIORITY_HIGH_TO_LOW -> filteredTasks.sortedByDescending { it.priority.level }
                    TaskSort.PRIORITY_LOW_TO_HIGH -> filteredTasks.sortedBy { it.priority.level }
                    TaskSort.CREATED_NEW_FIRST -> filteredTasks.sortedByDescending { it.createdAt }
                    TaskSort.CREATED_OLD_FIRST -> filteredTasks.sortedBy { it.createdAt }
                    TaskSort.ALPHABETICAL -> filteredTasks.sortedBy { it.title.lowercase() }
                }

                /**
                 * Calculate counts
                 */
                val completedCount = tasks.count { it.isCompleted }

                /**
                 * Create UI state
                 */
                TaskManagerUiState.Success(
                    allTasks = tasks,
                    displayedTasks = sortedTasks,
                    selectedFilter = filter,
                    selectedSort = sort,
                    searchQuery = search,
                    pendingCount = pendingCount,
                    completedCount = completedCount,
                    overdueCount = overdueCount
                )
            }
                .catch { error ->
                    /**
                     * Handle errors
                     */
                    emit(TaskManagerUiState.Error(
                        error.message ?: "Failed to load tasks"
                    ))
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * Change filter
     * State change triggers combine() to re-emit
     */
    private fun changeFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
        _showFilterSheet.value = false
    }

    /**
     * Change sort
     */
    private fun changeSort(sort: TaskSort) {
        _selectedSort.value = sort
        _showSortSheet.value = false
    }

    /**
     * Update search query
     */
    private fun search(query: String) {
        _searchQuery.value = query
    }

    /**
     * Clear search
     */
    private fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Toggle task completion
     */
    private fun toggleCompletion(taskId: Long) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId)
            // UI updates automatically via Flow
        }
    }

    /**
     * Delete task with undo support
     * 
     * UNDO PATTERN:
     * 1. Delete from DB
     * 2. Show snackbar with undo action
     * 3. If undo clicked, re-insert task
     * 4. If snackbar dismissed, deletion permanent
     */
    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            // Undo handled by UI via event
        }
    }

    /**
     * Undo delete
     * Re-insert task into database
     */
    private fun undoDelete(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }

    /**
     * Delete all completed tasks
     * 
     * BULK DELETE:
     * Filter completed tasks and delete each
     */
    private fun deleteAllCompleted() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TaskManagerUiState.Success) {
                val completedTasks = currentState.allTasks.filter { it.isCompleted }
                completedTasks.forEach { task ->
                    taskRepository.deleteTask(task)
                }
            }
        }
    }

    /**
     * Dismiss bottom sheets
     */
    fun dismissFilterSheet() {
        _showFilterSheet.value = false
    }

    fun dismissSortSheet() {
        _showSortSheet.value = false
    }
}
