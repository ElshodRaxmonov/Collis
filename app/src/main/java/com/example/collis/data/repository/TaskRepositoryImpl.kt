package com.example.collis.data.repository


import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.data.local.dao.TaskDao
import com.example.collis.data.local.entity.TaskEntity
import com.example.collis.data.local.entity.TaskPriority as EntityPriority
import com.example.collis.data.local.entity.RepeatType as EntityRepeatType
import com.example.collis.domain.model.Task
import com.example.collis.domain.model.TaskPriority
import com.example.collis.domain.model.RepeatType
import com.example.collis.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository
 *
 * IMPLEMENTATION DETAILS:
 * - Lives in DATA layer (depends on Room)
 * - Implements interface from DOMAIN layer
 * - Handles data mapping: Entity ↔ Domain Model
 * - Single source of truth for task operations
 *
 * @Singleton: Only one instance across app
 * @Inject: Hilt provides dependencies automatically
 *
 * MAPPING PATTERN:
 * Why convert Entity to Domain Model?
 * - Entity has Room annotations (@ColumnInfo, etc.)
 * - Domain Model is pure Kotlin (no framework dependencies)
 * - Allows changing database without touching business logic
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    /**
     * FLOW MAPPING EXPLAINED:
     *
     * Flow<List<TaskEntity>> from DAO
     *     ↓ .map { } transforms each emission
     * Flow<List<Task>> for domain layer
     *
     * Every time database changes:
     * 1. DAO emits new List<TaskEntity>
     * 2. .map {} converts to List<Task>
     * 3. UI receives updated List<Task>
     *
     * All automatic, no manual refresh needed!
     */

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getPendingTasks(): Flow<List<Task>> {
        return taskDao.getPendingTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getCompletedTasks(): Flow<List<Task>> {
        return taskDao.getCompletedTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getUpcomingTasks(limit: Int): Flow<List<Task>> {
        return taskDao.getUpcomingTasks(
            today = LocalDateTime.now(),
            limit = limit
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getTasksBySubject(subjectCode: String): Flow<List<Task>> {
        return taskDao.getTasksBySubject(subjectCode).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toDomainModel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    /**
     * Business logic implementation
     * Toggles completion status with timestamp
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun toggleTaskCompletion(taskId: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        val newCompletionStatus = !task.isCompleted
        val completionTime = if (newCompletionStatus) LocalDateTime.now() else null

        taskDao.updateTaskCompletion(
            taskId = taskId,
            completed = newCompletionStatus,
            completedAt = completionTime
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTasksWithReminders(): List<Task> {
        return taskDao.getTasksWithReminders().map { it.toDomainModel() }
    }

    override fun getPendingTaskCount(): Flow<Int> {
        return taskDao.getPendingTaskCount()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getOverdueTaskCount(): Flow<Int> {
        return taskDao.getOverdueTaskCount(LocalDateTime.now())
    }
}

/**
 * MAPPER EXTENSION FUNCTIONS
 *
 * WHY Extension Functions?
 * ✅ Keep mapping logic close to models
 * ✅ Reusable across repository
 * ✅ Clean and readable
 * ✅ No utility class needed
 *
 * LEARNING: Extension functions add methods to existing classes
 * without modifying them or using inheritance
 */

// Convert Entity to Domain Model
@RequiresApi(Build.VERSION_CODES.O)
private fun TaskEntity.toDomainModel(): Task {
    return Task(
        id = this.id,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        reminderTime = this.reminderTime,
        priority = this.priority.toDomainPriority(),
        isCompleted = this.isCompleted,
        subjectCode = this.subjectCode,
        createdAt = this.createdAt,
        completedAt = this.completedAt,
        repeatEnabled = this.repeatEnabled,
        repeatType = this.repeatType.toDomainRepeatType()
    )
}

// Convert Domain Model to Entity
@RequiresApi(Build.VERSION_CODES.O)
private fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        reminderTime = this.reminderTime,
        priority = this.priority.toEntityPriority(),
        isCompleted = this.isCompleted,
        subjectCode = this.subjectCode,
        createdAt = this.createdAt,
        completedAt = this.completedAt,
        repeatEnabled = this.repeatEnabled,
        repeatType = this.repeatType.toEntityRepeatType()
    )
}

// Priority mapping
private fun EntityPriority.toDomainPriority(): TaskPriority {
    return when (this) {
        EntityPriority.LOW -> TaskPriority.LOW
        EntityPriority.MEDIUM -> TaskPriority.MEDIUM
        EntityPriority.HIGH -> TaskPriority.HIGH
        EntityPriority.URGENT -> TaskPriority.URGENT
    }
}

private fun TaskPriority.toEntityPriority(): EntityPriority {
    return when (this) {
        TaskPriority.LOW -> EntityPriority.LOW
        TaskPriority.MEDIUM -> EntityPriority.MEDIUM
        TaskPriority.HIGH -> EntityPriority.HIGH
        TaskPriority.URGENT -> EntityPriority.URGENT
    }
}

// RepeatType mapping
private fun EntityRepeatType.toDomainRepeatType(): RepeatType {
    return when (this) {
        EntityRepeatType.NONE -> RepeatType.NONE
        EntityRepeatType.DAILY -> RepeatType.DAILY
        EntityRepeatType.WEEKLY -> RepeatType.WEEKLY
        EntityRepeatType.MONTHLY -> RepeatType.MONTHLY
    }
}

private fun RepeatType.toEntityRepeatType(): EntityRepeatType {
    return when (this) {
        RepeatType.NONE -> EntityRepeatType.NONE
        RepeatType.DAILY -> EntityRepeatType.DAILY
        RepeatType.WEEKLY -> EntityRepeatType.WEEKLY
        RepeatType.MONTHLY -> EntityRepeatType.MONTHLY
    }
}