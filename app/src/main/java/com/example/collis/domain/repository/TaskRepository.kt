package com.example.collis.domain.repository



import com.example.collis.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Task Repository Interface (Domain Layer)
 *
 * REPOSITORY PATTERN EXPLAINED:
 * - Mediates between domain and data layers
 * - Provides clean API for data access
 * - Abstracts data source (could be Room, API, both, or cache)
 * - Single source of truth for data operations
 *
 * WHY INTERFACE in Domain?
 * ✅ Dependency Inversion Principle (SOLID)
 * ✅ Domain doesn't depend on implementation details
 * ✅ Easy to mock for testing
 * ✅ Can swap implementations (Room → Firebase, etc.)
 *
 * IMPLEMENTATION lives in Data layer
 * Interface lives in Domain layer
 * This is "Dependency Inversion" - high-level module (domain)
 * doesn't depend on low-level module (data)
 */
interface TaskRepository {

    /**
     * FLOW RETURN TYPE:
     * - Reactive stream of data
     * - UI automatically updates when database changes
     * - No need to manually refresh
     *
     * Example: Adding a task triggers Flow emission,
     * UI collecting this Flow automatically shows new task
     */

    fun getAllTasks(): Flow<List<Task>>

    fun getPendingTasks(): Flow<List<Task>>

    fun getCompletedTasks(): Flow<List<Task>>

    fun getUpcomingTasks(limit: Int = 5): Flow<List<Task>>

    fun getTasksBySubject(subjectCode: String): Flow<List<Task>>

    /**
     * SUSPEND FUNCTION:
     * - Can only be called from coroutines
     * - Doesn't block calling thread
     * - Perfect for one-time operations
     */
    suspend fun getTaskById(taskId: Long): Task?

    suspend fun insertTask(task: Task): Long

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun toggleTaskCompletion(taskId: Long)

    suspend fun getTasksWithReminders(): List<Task>

    /**
     * Get statistics for dashboard
     * LEARNING: Repository can provide computed data
     */
    fun getPendingTaskCount(): Flow<Int>

    fun getOverdueTaskCount(): Flow<Int>
}

/**
 * USAGE in ViewModel:
 *
 * class TaskViewModel(private val repository: TaskRepository) {
 *     // Automatically updates UI
 *     val tasks = repository.getAllTasks()
 *         .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
 *
 *     fun addTask(task: Task) = viewModelScope.launch {
 *         repository.insertTask(task)
 *         // No need to manually update UI - Flow handles it
 *     }
 * }
 */