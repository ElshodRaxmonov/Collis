package com.example.collis.presentation.ui.student.task

import com.example.collis.domain.model.RepeatType
import com.example.collis.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


/**
 * Add/Edit Task UI Events
 */
sealed class AddEditTaskUiEvent {
    /**
     * Field Changes
     */
    data class TitleChanged(val title: String) : AddEditTaskUiEvent()
    data class DescriptionChanged(val description: String) : AddEditTaskUiEvent()
    data class DueDateChanged(val date: LocalDate?) : AddEditTaskUiEvent()
    data class DueTimeChanged(val time: LocalTime?) : AddEditTaskUiEvent()
    data class ReminderDateChanged(val date: LocalDate?) : AddEditTaskUiEvent()
    data class ReminderTimeChanged(val time: LocalTime?) : AddEditTaskUiEvent()
    data class PriorityChanged(val priority: TaskPriority) : AddEditTaskUiEvent()
    data class SubjectChanged(val subjectCode: String?) : AddEditTaskUiEvent()
    data class RepeatEnabledChanged(val enabled: Boolean) : AddEditTaskUiEvent()
    data class RepeatTypeChanged(val type: RepeatType) : AddEditTaskUiEvent()

    /**
     * Picker Actions
     */
    data object ShowDueDatePicker : AddEditTaskUiEvent()
    data object HideDueDatePicker : AddEditTaskUiEvent()
    data object ShowDueTimePicker : AddEditTaskUiEvent()
    data object HideDueTimePicker : AddEditTaskUiEvent()
    data object ShowReminderDatePicker : AddEditTaskUiEvent()
    data object HideReminderDatePicker : AddEditTaskUiEvent()
    data object ShowReminderTimePicker : AddEditTaskUiEvent()
    data object HideReminderTimePicker : AddEditTaskUiEvent()
    data object ShowPrioritySelector : AddEditTaskUiEvent()
    data object HidePrioritySelector : AddEditTaskUiEvent()
    data object ShowSubjectSelector : AddEditTaskUiEvent()
    data object HideSubjectSelector : AddEditTaskUiEvent()
    data object ShowRepeatSelector : AddEditTaskUiEvent()
    data object HideRepeatSelector : AddEditTaskUiEvent()

    /**
     * Quick Actions
     */
    data object ClearDueDate : AddEditTaskUiEvent()
    data object ClearReminder : AddEditTaskUiEvent()
    data object SetDueDateToday : AddEditTaskUiEvent()
    data object SetDueDateTomorrow : AddEditTaskUiEvent()

    /**
     * Form Actions
     */
    data object SaveTask : AddEditTaskUiEvent()
    data object DeleteTask : AddEditTaskUiEvent()
    data object Cancel : AddEditTaskUiEvent()
}

/**
 * One-time events
 */
sealed class AddEditTaskOneTimeEvent {
    data object NavigateBack : AddEditTaskOneTimeEvent()
    data class ShowSnackbar(val message: String) : AddEditTaskOneTimeEvent()
    data class ScheduleReminder(val taskId: Long, val reminderTime: LocalDateTime) : AddEditTaskOneTimeEvent()
}