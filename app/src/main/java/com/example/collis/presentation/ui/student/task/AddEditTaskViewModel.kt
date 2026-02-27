package com.example.collis.presentation.ui.student.task


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.Task
import com.example.collis.domain.model.TaskPriority
import com.example.collis.domain.model.RepeatType
import com.example.collis.domain.repository.LessonRepository
import com.example.collis.domain.repository.TaskRepository
import com.example.collis.domain.usecase.task.AddTaskUseCase
import com.example.collis.presentation.ui.student.task.AddEditTaskUiEvent
import com.example.collis.presentation.ui.student.task.AddEditTaskUiState

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Add/Edit Task ViewModel
 *
 * IMPROVEMENTS:
 * - Load unique subjects from LessonRepository for selection.
 * - Handle pre-selected subject from navigation.
 */
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val lessonRepository: LessonRepository,
    private val addTaskUseCase: AddTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long? = savedStateHandle.get<String>("taskId")?.toLongOrNull()
    private val initialSubjectCode: String? = savedStateHandle.get<String>("subjectCode")

    /**
     * UI State
     */
    private val _uiState = MutableStateFlow(
        AddEditTaskUiState(
            isEditMode = taskId != null,
            taskId = taskId,
            subjectCode = initialSubjectCode
        )
    )
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    /**
     * List of available subjects for the selector
     */
    private val _availableSubjects = MutableStateFlow<List<String>>(emptyList())
    val availableSubjects: StateFlow<List<String>> = _availableSubjects.asStateFlow()

    /**
     * One-time events
     */
    private val _oneTimeEvent = Channel<AddEditTaskOneTimeEvent>(Channel.BUFFERED)
    val oneTimeEvent = _oneTimeEvent.receiveAsFlow()

    init {
        if (taskId != null) {
            loadTask(taskId)
        }
        loadAvailableSubjects()
    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            // Fetch all lessons to extract unique subject codes
            lessonRepository.getTodayLessons().collect { result ->
                if (result is NetworkResult.Success) {
                    val subjects = result.data?.map { it.courseCode }?.distinct() ?: emptyList()
                    _availableSubjects.value = subjects
                }
            }
        }
    }

    /**
     * Event Handler
     */
    fun onEvent(event: AddEditTaskUiEvent) {
        when (event) {
            is AddEditTaskUiEvent.TitleChanged -> updateTitle(event.title)
            is AddEditTaskUiEvent.DescriptionChanged -> updateDescription(event.description)
            is AddEditTaskUiEvent.DueDateChanged -> updateDueDate(event.date)
            is AddEditTaskUiEvent.DueTimeChanged -> updateDueTime(event.time)
            is AddEditTaskUiEvent.ReminderDateChanged -> updateReminderDate(event.date)
            is AddEditTaskUiEvent.ReminderTimeChanged -> updateReminderTime(event.time)
            is AddEditTaskUiEvent.PriorityChanged -> updatePriority(event.priority)
            is AddEditTaskUiEvent.SubjectChanged -> updateSubject(event.subjectCode)
            is AddEditTaskUiEvent.RepeatEnabledChanged -> updateRepeatEnabled(event.enabled)
            is AddEditTaskUiEvent.RepeatTypeChanged -> updateRepeatType(event.type)

            // Pickers
            is AddEditTaskUiEvent.ShowDueDatePicker -> _uiState.update { it.copy(showDueDatePicker = true) }
            is AddEditTaskUiEvent.HideDueDatePicker -> _uiState.update { it.copy(showDueDatePicker = false) }
            is AddEditTaskUiEvent.ShowDueTimePicker -> _uiState.update { it.copy(showDueTimePicker = true) }
            is AddEditTaskUiEvent.HideDueTimePicker -> _uiState.update { it.copy(showDueTimePicker = false) }
            is AddEditTaskUiEvent.ShowReminderDatePicker -> _uiState.update { it.copy(showReminderDatePicker = true) }
            is AddEditTaskUiEvent.HideReminderDatePicker -> _uiState.update { it.copy(showReminderDatePicker = false) }
            is AddEditTaskUiEvent.ShowReminderTimePicker -> _uiState.update { it.copy(showReminderTimePicker = true) }
            is AddEditTaskUiEvent.HideReminderTimePicker -> _uiState.update { it.copy(showReminderTimePicker = false) }
            is AddEditTaskUiEvent.ShowPrioritySelector -> _uiState.update { it.copy(showPrioritySelector = true) }
            is AddEditTaskUiEvent.HidePrioritySelector -> _uiState.update { it.copy(showPrioritySelector = false) }
            is AddEditTaskUiEvent.ShowSubjectSelector -> _uiState.update { it.copy(showSubjectSelector = true) }
            is AddEditTaskUiEvent.HideSubjectSelector -> _uiState.update { it.copy(showSubjectSelector = false) }
            is AddEditTaskUiEvent.ShowRepeatSelector -> _uiState.update { it.copy(showRepeatSelector = true) }
            is AddEditTaskUiEvent.HideRepeatSelector -> _uiState.update { it.copy(showRepeatSelector = false) }

            // Quick Actions
            is AddEditTaskUiEvent.ClearDueDate -> clearDueDate()
            is AddEditTaskUiEvent.ClearReminder -> clearReminder()
            is AddEditTaskUiEvent.SetDueDateToday -> setDueDateToday()
            is AddEditTaskUiEvent.SetDueDateTomorrow -> setDueDateTomorrow()

            // Form Actions
            is AddEditTaskUiEvent.SaveTask -> saveTask()
            is AddEditTaskUiEvent.DeleteTask -> deleteTask()
            is AddEditTaskUiEvent.Cancel -> cancel()
        }
    }

    private fun loadTask(taskId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val task = taskRepository.getTaskById(taskId)
            if (task != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        title = task.title,
                        description = task.description ?: "",
                        dueDate = task.dueDate?.toLocalDate(),
                        dueTime = task.dueDate?.toLocalTime(),
                        reminderDate = task.reminderTime?.toLocalDate(),
                        reminderTime = task.reminderTime?.toLocalTime(),
                        priority = task.priority,
                        subjectCode = task.subjectCode,
                        repeatEnabled = task.repeatEnabled,
                        repeatType = task.repeatType
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
                _oneTimeEvent.send(AddEditTaskOneTimeEvent.ShowSnackbar("Task not found"))
                _oneTimeEvent.send(AddEditTaskOneTimeEvent.NavigateBack)
            }
        }
    }

    private fun updateTitle(title: String) {
        val error = when {
            title.isBlank() -> "Title is required"
            title.length > 100 -> "Title too long"
            else -> null
        }
        _uiState.update { it.copy(title = title, titleError = error) }
    }

    private fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    private fun updateDueDate(date: LocalDate?) {
        _uiState.update {
            it.copy(
                dueDate = date,
                showDueDatePicker = false
            )
        }
        validateReminder()
    }

    private fun updateDueTime(time: LocalTime?) {
        _uiState.update {
            it.copy(
                dueTime = time,
                showDueTimePicker = false
            )
        }
        validateReminder()
    }

    private fun updateReminderDate(date: LocalDate?) {
        _uiState.update {
            it.copy(
                reminderDate = date,
                showReminderDatePicker = false
            )
        }
        validateReminder()
    }

    private fun updateReminderTime(time: LocalTime?) {
        _uiState.update {
            it.copy(
                reminderTime = time,
                showReminderTimePicker = false
            )
        }
        validateReminder()
    }

    private fun validateReminder() {
        val state = _uiState.value
        if (state.reminderDateTime != null && state.dueDateTime != null) {
            val error = if (state.reminderDateTime!!.isAfter(state.dueDateTime)) {
                "Reminder must be before due date"
            } else null
            _uiState.update { it.copy(reminderError = error) }
        } else {
            _uiState.update { it.copy(reminderError = null) }
        }
    }

    private fun updatePriority(priority: TaskPriority) {
        _uiState.update {
            it.copy(
                priority = priority,
                showPrioritySelector = false
            )
        }
    }

    private fun updateSubject(subjectCode: String?) {
        _uiState.update {
            it.copy(
                subjectCode = subjectCode,
                showSubjectSelector = false
            )
        }
    }

    private fun updateRepeatEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                repeatEnabled = enabled,
                repeatType = if (!enabled) RepeatType.NONE else it.repeatType
            )
        }
    }

    private fun updateRepeatType(type: RepeatType) {
        _uiState.update {
            it.copy(
                repeatType = type,
                showRepeatSelector = false
            )
        }
    }

    private fun clearDueDate() {
        _uiState.update { it.copy(dueDate = null, dueTime = null) }
    }

    private fun clearReminder() {
        _uiState.update { it.copy(reminderDate = null, reminderTime = null) }
    }

    private fun setDueDateToday() {
        updateDueDate(LocalDate.now())
    }

    private fun setDueDateTomorrow() {
        updateDueDate(LocalDate.now().plusDays(1))
    }

    private fun saveTask() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isValid) {
                _oneTimeEvent.send(AddEditTaskOneTimeEvent.ShowSnackbar("Please fix errors"))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }
            try {
                val task = Task(
                    id = state.taskId ?: 0,
                    title = state.title.trim(),
                    description = state.description.trim().takeIf { it.isNotBlank() },
                    dueDate = state.dueDateTime,
                    reminderTime = state.reminderDateTime,
                    priority = state.priority,
                    subjectCode = state.subjectCode,
                    repeatEnabled = state.repeatEnabled,
                    repeatType = state.repeatType,
                    isCompleted = false
                )

                val taskId = if (state.isEditMode) {
                    taskRepository.updateTask(task)
                    state.taskId!!
                } else {
                    taskRepository.insertTask(task)
                }

                if (state.reminderDateTime != null) {
                    _oneTimeEvent.send(AddEditTaskOneTimeEvent.ScheduleReminder(taskId, state.reminderDateTime!!))
                }

                _oneTimeEvent.send(AddEditTaskOneTimeEvent.ShowSnackbar(if (state.isEditMode) "Task updated" else "Task created"))
                _oneTimeEvent.send(AddEditTaskOneTimeEvent.NavigateBack)
            } catch (e: Exception) {
                _oneTimeEvent.send(AddEditTaskOneTimeEvent.ShowSnackbar(e.message ?: "Error saving task"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.taskId != null) {
                val task = taskRepository.getTaskById(state.taskId)
                if (task != null) {
                    taskRepository.deleteTask(task)
                    _oneTimeEvent.send(AddEditTaskOneTimeEvent.ShowSnackbar("Task deleted"))
                    _oneTimeEvent.send(AddEditTaskOneTimeEvent.NavigateBack)
                }
            }
        }
    }

    private fun cancel() {
        viewModelScope.launch {
            _oneTimeEvent.send(AddEditTaskOneTimeEvent.NavigateBack)
        }
    }
}