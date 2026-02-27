package com.example.collis.domain.usecase.task


import com.example.collis.domain.model.Task
import com.example.collis.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use Case for Adding Tasks
 *
 * USE CASE PATTERN EXPLAINED:
 * - Represents single business operation
 * - Encapsulates business logic
 * - Reusable across ViewModels
 * - Easy to test in isolation
 * - Follows Single Responsibility Principle
 *
 * WHY Use Cases?
 * ❌ Without: ViewModels become bloated with business logic
 * ✅ With: Clean ViewModels, testable logic, reusable operations
 *
 * STRUCTURE:
 * - operator fun invoke(): Makes class callable like function
 * - Single public method for clarity
 * - Dependencies injected via constructor
 *
 * LEARNING: "invoke" operator lets you call class instance as function
 * Example: val useCase = AddTaskUseCase(repo)
 *          useCase(task) // instead of useCase.execute(task)
 */
class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Add task with validation
     *
     * @param task Task to add
     * @return Task ID if successful
     * @throws IllegalArgumentException if validation fails
     *
     * BUSINESS RULES:
     * 1. Title must not be blank
     * 2. Due date must be in future (if provided)
     * 3. Reminder must be before due date
     */
    suspend operator fun invoke(task: Task): Result<Long> {
        return try {
            // Validation
            if (task.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Task title cannot be empty"))
            }

            if (task.dueDate != null && task.dueDate.isBefore(task.createdAt)) {
                return Result.failure(IllegalArgumentException("Due date cannot be in the past"))
            }

            if (task.reminderTime != null && task.dueDate != null) {
                if (task.reminderTime.isAfter(task.dueDate)) {
                    return Result.failure(
                        IllegalArgumentException("Reminder must be before due date")
                    )
                }
            }

            // Insert task
            val taskId = repository.insertTask(task)
            Result.success(taskId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * USAGE in ViewModel:
 *
 * class TaskViewModel(private val addTaskUseCase: AddTaskUseCase) {
 *     fun addTask(task: Task) = viewModelScope.launch {
 *         val result = addTaskUseCase(task)
 *         result.onSuccess { taskId ->
 *             _uiState.value = UiState.Success("Task added!")
 *         }
 *         result.onFailure { error ->
 *             _uiState.value = UiState.Error(error.message)
 *         }
 *     }
 * }
 */