package com.example.collis.domain.usecase.task

import com.example.collis.domain.model.Task
import com.example.collis.domain.repository.TaskRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use Case for fetching upcoming tasks with business logic
 *
 * WHAT IT DOES:
 * - Gets pending tasks due within next 7 days
 * - Sorts by priority and due date
 * - Filters out overdue tasks
 *
 * BUSINESS VALUE:
 * Shows students what to focus on next
 */
class GetUpcomingTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(limit: Int = 5): Flow<List<Task>> {
        val sevenDaysFromNow = LocalDateTime.now().plusDays(7)

        return repository.getAllTasks().map { tasks ->
            tasks
                .filter { !it.isCompleted && it.dueDate != null }
                .filter { it.dueDate!!.isBefore(sevenDaysFromNow) }
                .filter { it.dueDate!!.isAfter(LocalDateTime.now()) } // Exclude overdue
                .sortedWith(
                    compareByDescending<Task> { it.priority.level }
                        .thenBy { it.dueDate }
                )
                .take(limit)
        }
    }
}

/**
 * CHAINING OPERATORS EXPLAINED:
 * .filter { } - keeps only items matching condition
 * .sortedWith { } - custom sorting logic
 * .take(n) - limits to first n items
 *
 * FLUENT API PATTERN: Clean, readable data transformations
 */