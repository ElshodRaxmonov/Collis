package com.example.collis.data.local.entity


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Room Entity for Student Tasks
 *
 * Features:
 * - Task creation with optional reminders
 * - Completion tracking
 * - Priority levels
 * - Due date management
 */
@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: LocalDateTime? = null,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: LocalDateTime? = null,

    @ColumnInfo(name = "priority")
    val priority: TaskPriority = TaskPriority.MEDIUM,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "subject_code")
    val subjectCode: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "completed_at")
    val completedAt: LocalDateTime? = null,

    @ColumnInfo(name = "repeat_enabled")
    val repeatEnabled: Boolean = false,

    @ColumnInfo(name = "repeat_type")
    val repeatType: RepeatType = RepeatType.NONE
)

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}