package com.example.collis.data.local.dao


import androidx.room.*
import com.example.collis.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Task operations
 *
 * Industry best practices:
 * - Flow for reactive UI updates
 * - Suspend functions for coroutine support
 * - Efficient querying with indexes
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY due_date ASC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY due_date ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE due_date >= :startDate AND due_date < :endDate ORDER BY due_date ASC")
    fun getTasksForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE due_date >= :today ORDER BY due_date ASC LIMIT :limit")
    fun getUpcomingTasks(today: LocalDateTime, limit: Int = 5): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE subject_code = :subjectCode ORDER BY due_date ASC")
    fun getTasksBySubject(subjectCode: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE reminder_time IS NOT NULL AND is_completed = 0")
    suspend fun getTasksWithReminders(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    // In TaskDao.kt
    @Update
    suspend fun updateTask(task: TaskEntity): Int // Returns number of rows updated

    @Delete
    suspend fun deleteTask(task: TaskEntity): Int // Returns number of rows deleted

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("UPDATE tasks SET is_completed = :completed, completed_at = :completedAt WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, completed: Boolean, completedAt: LocalDateTime?)

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    fun getPendingTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0 AND due_date < :now")
    fun getOverdueTaskCount(now: LocalDateTime): Flow<Int>

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}