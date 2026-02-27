package com.example.collis.presentation.ui.student.task


import com.example.collis.domain.model.RepeatType
import com.example.collis.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Add/Edit Task UI State
 *
 * FORM STATE PATTERN:
 * - Separate fields for each input
 * - Validation errors per field
 * - Form-level validation (isValid)
 * - Loading state during save
 *
 * LEARNING: Complex forms need structured state
 * Each field has value + error state
 */
data class AddEditTaskUiState(
    /**
     * Mode: Add or Edit
     */
    val isEditMode: Boolean = false,
    val taskId: Long? = null,

    /**
     * Form Fields
     */
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderDate: LocalDate? = null,
    val reminderTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val subjectCode: String? = null,
    val repeatEnabled: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE,

    /**
     * Field Errors
     */
    val titleError: String? = null,
    val dueDateError: String? = null,
    val reminderError: String? = null,

    /**
     * UI State
     */
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    /**
     * Pickers State
     */
    val showDueDatePicker: Boolean = false,
    val showDueTimePicker: Boolean = false,
    val showReminderDatePicker: Boolean = false,
    val showReminderTimePicker: Boolean = false,
    val showPrioritySelector: Boolean = false,
    val showSubjectSelector: Boolean = false,
    val showRepeatSelector: Boolean = false
) {
    /**
     * Form validation
     *
     * BUSINESS RULES:
     * - Title required
     * - Due date optional but must be future
     * - Reminder must be before due date
     * - Reminder requires due date
     */
    val isValid: Boolean
        get() = title.isNotBlank() &&
                titleError == null &&
                dueDateError == null &&
                reminderError == null

    /**
     * Has reminder set
     */
    val hasReminder: Boolean
        get() = reminderDate != null && reminderTime != null

    /**
     * Combined due date-time
     */
    val dueDateTime: LocalDateTime?
        get() = if (dueDate != null && dueTime != null) {
            LocalDateTime.of(dueDate, dueTime)
        } else if (dueDate != null) {
            LocalDateTime.of(dueDate, LocalTime.of(23, 59))
        } else {
            null
        }

    /**
     * Combined reminder date-time
     */
    val reminderDateTime: LocalDateTime?
        get() = if (reminderDate != null && reminderTime != null) {
            LocalDateTime.of(reminderDate, reminderTime)
        } else {
            null
        }
}
