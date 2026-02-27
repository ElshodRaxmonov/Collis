package com.example.collis.presentation.ui.student.task


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collis.core.util.AlarmScheduler
import com.example.collis.domain.model.TaskPriority
import com.example.collis.presentation.components.*
import com.example.collis.ui.theme.CollisTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Add/Edit Task Screen
 * 
 * FORM COMPONENTS:
 * - Text fields (title, description)
 * - Date/Time pickers (due date, reminder)
 * - Selector dialogs (priority, subject)
 *
 * UX IMPROVEMENTS:
 * - Dynamic subject list fetched from student's schedule.
 * - Responsive layout with consistent padding.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long?,
    onNavigateBack: () -> Unit,
    onTaskSaved: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableSubjects by viewModel.availableSubjects.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.oneTimeEvent.collect { event ->
            when (event) {
                is AddEditTaskOneTimeEvent.NavigateBack -> {
                    onTaskSaved()
                    onNavigateBack()
                }
                is AddEditTaskOneTimeEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AddEditTaskOneTimeEvent.ScheduleReminder -> {
                    AlarmScheduler.scheduleTaskReminder(
                        context = context,
                        taskId = event.taskId,
                        taskTitle = uiState.title,
                        taskDescription = uiState.description,
                        reminderTime = event.reminderTime
                    )
                }
            }
        }
    }

    AddEditTaskScreenContent(
        uiState = uiState,
        availableSubjects = availableSubjects,
        snackbarHostState = snackbarHostState,
        onEvent = { viewModel.onEvent(it) }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTaskScreenContent(
    uiState: AddEditTaskUiState,
    availableSubjects: List<String>,
    snackbarHostState: SnackbarHostState,
    onEvent: (AddEditTaskUiEvent) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CollisTopBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Task" else "New Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AddEditTaskUiEvent.Cancel) }) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    TextButton(
                        onClick = { onEvent(AddEditTaskUiEvent.SaveTask) },
                        enabled = uiState.isValid && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                CollisTextField(
                    value = uiState.title,
                    onValueChange = { onEvent(AddEditTaskUiEvent.TitleChanged(it)) },
                    label = "Title",
                    placeholder = "What needs to be done?",
                    leadingIcon = Icons.AutoMirrored.Filled.Assignment,
                    isError = uiState.titleError != null,
                    errorMessage = uiState.titleError,
                    singleLine = true
                )

                // Description
                MultilineTextField(
                    value = uiState.description,
                    onValueChange = { onEvent(AddEditTaskUiEvent.DescriptionChanged(it)) },
                    label = "Description",
                    placeholder = "Add details... (optional)",
                    minLines = 3
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                // Priority
                SectionHeader(title = "Priority")
                PrioritySelector(
                    selectedPriority = uiState.priority,
                    onPriorityClick = { onEvent(AddEditTaskUiEvent.ShowPrioritySelector) }
                )

                // Subject
                SectionHeader(title = "Subject", subtitle = "Link this task to a subject")
                SubjectSelector(
                    selectedSubject = uiState.subjectCode,
                    onClick = { onEvent(AddEditTaskUiEvent.ShowSubjectSelector) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                // Due Date
                SectionHeader(title = "Due Date")
                DueDateSection(
                    dueDate = uiState.dueDate,
                    dueTime = uiState.dueTime,
                    error = uiState.dueDateError,
                    onDateClick = { onEvent(AddEditTaskUiEvent.ShowDueDatePicker) },
                    onTimeClick = { onEvent(AddEditTaskUiEvent.ShowDueTimePicker) },
                    onClear = { onEvent(AddEditTaskUiEvent.ClearDueDate) },
                    onTodayClick = { onEvent(AddEditTaskUiEvent.SetDueDateToday) },
                    onTomorrowClick = { onEvent(AddEditTaskUiEvent.SetDueDateTomorrow) }
                )

                // Reminder
                SectionHeader(title = "Reminder", subtitle = "Set a wakeup alarm")
                ReminderSection(
                    reminderDate = uiState.reminderDate,
                    reminderTime = uiState.reminderTime,
                    error = uiState.reminderError,
                    enabled = uiState.dueDate != null,
                    onDateClick = { onEvent(AddEditTaskUiEvent.ShowReminderDatePicker) },
                    onTimeClick = { onEvent(AddEditTaskUiEvent.ShowReminderTimePicker) },
                    onClear = { onEvent(AddEditTaskUiEvent.ClearReminder) }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        /**
         * Dialogs
         */
        if (uiState.showDueDatePicker) {
            DatePickerDialog(
                initialDate = uiState.dueDate ?: LocalDate.now(),
                onDateSelected = { date -> onEvent(AddEditTaskUiEvent.DueDateChanged(date)) },
                onDismiss = { onEvent(AddEditTaskUiEvent.HideDueDatePicker) }
            )
        }

        if (uiState.showDueTimePicker) {
            TimePickerDialog(
                initialTime = uiState.dueTime ?: LocalTime.now(),
                onTimeSelected = { time -> onEvent(AddEditTaskUiEvent.DueTimeChanged(time)) },
                onDismiss = { onEvent(AddEditTaskUiEvent.HideDueTimePicker) }
            )
        }

        if (uiState.showReminderDatePicker) {
            DatePickerDialog(
                initialDate = uiState.reminderDate ?: LocalDate.now(),
                onDateSelected = { date -> onEvent(AddEditTaskUiEvent.ReminderDateChanged(date)) },
                onDismiss = { onEvent(AddEditTaskUiEvent.HideReminderDatePicker) }
            )
        }

        if (uiState.showReminderTimePicker) {
            TimePickerDialog(
                initialTime = uiState.reminderTime ?: LocalTime.now(),
                onTimeSelected = { time -> onEvent(AddEditTaskUiEvent.ReminderTimeChanged(time)) },
                onDismiss = { onEvent(AddEditTaskUiEvent.HideReminderTimePicker) }
            )
        }

        if (uiState.showPrioritySelector) {
            PrioritySelectorDialog(
                selectedPriority = uiState.priority,
                onPrioritySelected = { priority -> onEvent(AddEditTaskUiEvent.PriorityChanged(priority)) },
                onDismiss = { onEvent(AddEditTaskUiEvent.HidePrioritySelector) }
            )
        }

        if (uiState.showSubjectSelector) {
            SubjectSelectorDialog(
                subjects = availableSubjects,
                selectedSubject = uiState.subjectCode,
                onSubjectSelected = { subject -> onEvent(AddEditTaskUiEvent.SubjectChanged(subject)) },
                onDismiss = { onEvent(AddEditTaskUiEvent.HideSubjectSelector) }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Task?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onEvent(AddEditTaskUiEvent.DeleteTask)
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrioritySelector(selectedPriority: TaskPriority, onPriorityClick: () -> Unit) {
    OutlinedCard(onClick = onPriorityClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Priority Level", style = MaterialTheme.typography.bodyLarge)
            PriorityBadge(priority = selectedPriority)
        }
    }
}

@Composable
private fun SubjectSelector(selectedSubject: String?, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedSubject ?: "Select a subject",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedSubject == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                fontWeight = if (selectedSubject != null) FontWeight.SemiBold else FontWeight.Normal
            )
            Icon(Icons.Default.ArrowDropDown, null)
        }
    }
}

@Composable
private fun SubjectSelectorDialog(
    subjects: List<String>,
    selectedSubject: String?,
    onSubjectSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Subject") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                SubjectOption(
                    name = "None (No subject)",
                    isSelected = selectedSubject == null,
                    onClick = { onSubjectSelected(null) }
                )
                subjects.forEach { subject ->
                    SubjectOption(
                        name = subject,
                        isSelected = selectedSubject == subject,
                        onClick = { onSubjectSelected(subject) }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SubjectOption(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PrioritySelectorDialog(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Priority") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { priority ->
                    OutlinedCard(
                        onClick = { onPrioritySelected(priority) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (priority == selectedPriority)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = priority.displayName, style = MaterialTheme.typography.bodyLarge)
                            PriorityBadge(priority = priority)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun DueDateSection(
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    error: String?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onClear: () -> Unit,
    onTodayClick: () -> Unit,
    onTomorrowClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (dueDate == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(
                    onClick = onTodayClick,
                    label = { Text("Today") },
                    leadingIcon = { Icon(Icons.Default.Today, null, Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = onTomorrowClick,
                    label = { Text("Tomorrow") },
                    leadingIcon = { Icon(Icons.Default.Event, null, Modifier.size(18.dp)) }
                )
            }
        }
        OutlinedCard(onClick = onDateClick, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dueDate?.format(DateTimeFormatter.ofPattern("dd-MMM yyyy")) ?: "Set Due Date",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (dueDate == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (dueDate != null) {
                    IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            }
        }
        if (dueDate != null) {
            OutlinedCard(onClick = onTimeClick, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dueTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Set Time (Optional)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (dueTime == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderSection(
    reminderDate: LocalDate?,
    reminderTime: LocalTime?,
    error: String?,
    enabled: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onClear: () -> Unit
) {
    if (!enabled) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Text(
                text = "Set a due date first to enable reminders.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(onClick = onDateClick, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = reminderDate?.format(DateTimeFormatter.ofPattern("dd-MMM yyyy")) ?: "Set Alarm Date",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (reminderDate == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (reminderDate != null) IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Clear, null) }
                }
            }
            if (reminderDate != null) {
                OutlinedCard(onClick = onTimeClick, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Alarm, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = reminderTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Set Alarm Time",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (reminderTime == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onDateSelected(java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.of("UTC")).toLocalDate()) }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) { DatePicker(state = state) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialTime: LocalTime, onTimeSelected: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onTimeSelected(LocalTime.of(state.hour, state.minute)) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = state) }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddEditTaskScreenNewPreview() {
    CollisTheme {
        AddEditTaskScreenContent(
            uiState = AddEditTaskUiState(
                title = "",
                description = "",
                priority = TaskPriority.MEDIUM,
                isEditMode = false,
                isLoading = false
            ),
            availableSubjects = listOf("CS301", "CS302", "CS303"),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddEditTaskScreenEditPreview() {
    CollisTheme {
        AddEditTaskScreenContent(
            uiState = AddEditTaskUiState(
                title = "Complete Assignment",
                description = "Final report for mobile app dev",
                priority = TaskPriority.HIGH,
                subjectCode = "CS301",
                dueDate = LocalDate.now().plusDays(2),
                dueTime = LocalTime.of(23, 59),
                isEditMode = true,
                isLoading = false
            ),
            availableSubjects = listOf("CS301", "CS302", "CS303"),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
