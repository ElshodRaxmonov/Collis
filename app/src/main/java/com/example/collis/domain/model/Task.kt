package com.example.collis.domain.model

import android.os.Build
import androidx.annotation.RequiresApi


import java.time.LocalDateTime

/**
 * Domain Model for Task
 *
 * CLEAN ARCHITECTURE PRINCIPLE:
 * - Domain layer is the core of the app
 * - Contains business logic and rules
 * - Independent of frameworks (Room, Retrofit, etc.)
 * - Easy to test because no Android dependencies
 *
 * WHY SEPARATE from TaskEntity?
 * ✅ Separation of Concerns: Database structure ≠ Business logic
 * ✅ Flexibility: Can change database without affecting business logic
 * ✅ Testability: Pure Kotlin classes, no Android dependencies
 * ✅ Data mapping: Different representations for different layers
 *
 * MAPPING LAYERS:
 * Entity (Database) → Domain Model (Business Logic) → DTO (Network)
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDateTime? = null,
    val reminderTime: LocalDateTime? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val subjectCode: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val repeatEnabled: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE
) {
    /**
     * Business logic methods live in domain models
     * These are pure functions with no side effects
     */

    /**
     * Check if task is overdue
     * LEARNING: Business rules belong in domain, not UI or data layer
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isOverdue(): Boolean {
        return if (isCompleted || dueDate == null) {
            false
        } else {
            dueDate.isBefore(LocalDateTime.now())
        }
    }

    /**
     * Check if task is due today
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isDueToday(): Boolean {
        return dueDate?.toLocalDate() == LocalDateTime.now().toLocalDate()
    }

    /**
     * Get formatted time until due
     * Returns human-readable string like "2 hours", "3 days"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeUntilDue(): String? {
        if (dueDate == null || isCompleted) return null

        val now = LocalDateTime.now()
        val duration = java.time.Duration.between(now, dueDate)

        return when {
            duration.isNegative -> "Overdue"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes"
            duration.toHours() < 24 -> "${duration.toHours()} hours"
            duration.toDays() < 7 -> "${duration.toDays()} days"
            else -> "${duration.toDays() / 7} weeks"
        }
    }

    /**
     * Check if task needs reminder soon (within 1 hour)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun needsImmediateReminder(): Boolean {
        if (reminderTime == null || isCompleted) return false

        val now = LocalDateTime.now()
        val duration = java.time.Duration.between(now, reminderTime)

        return duration.toMinutes() in 0..60
    }

    /**
     * Validation business rule
     * LEARNING: Domain models can validate themselves
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                (dueDate == null || dueDate.isAfter(createdAt))
    }
}

/**
 * Priority levels for tasks
 * LEARNING: Enums provide type-safety and readability
 */
enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4);

    /**
     * Get color for each priority
     * This could be moved to UI layer, but simple color logic is acceptable here
     */
    fun getColorHex(): String {
        return when (this) {
            LOW -> "#4CAF50"      // Green
            MEDIUM -> "#FF9800"   // Orange
            HIGH -> "#F44336"     // Red
            URGENT -> "#D32F2F"   // Dark Red
        }
    }
}

enum class RepeatType(val displayName: String) {
    NONE("No Repeat"),
    DAILY("Every Day"),
    WEEKLY("Every Week"),
    MONTHLY("Every Month");

    /**
     * Calculate next occurrence based on repeat type
     * BUSINESS LOGIC in domain model
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextOccurrence(from: LocalDateTime): LocalDateTime? {
        return when (this) {
            NONE -> null
            DAILY -> from.plusDays(1)
            WEEKLY -> from.plusWeeks(1)
            MONTHLY -> from.plusMonths(1)
        }
    }
}