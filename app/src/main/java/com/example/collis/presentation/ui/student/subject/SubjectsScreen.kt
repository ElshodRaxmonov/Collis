package com.example.collis.presentation.ui.student.subject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collis.domain.model.LessonModel
import com.example.collis.presentation.components.*
import com.example.collis.ui.theme.CollisTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Schedule Screen - Core UI for viewing weekly timetable.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onNavigateToAddTask: (String) -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    SubjectsScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        onGoToToday = { viewModel.goToToday() },
        onDateSelected = { viewModel.selectDate(it) },
        onPreviousWeek = { viewModel.previousWeek() },
        onNextWeek = { viewModel.nextWeek() },
        onNavigateToAddTask = onNavigateToAddTask
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectsScreenContent(
    uiState: SubjectsUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onGoToToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onNavigateToAddTask: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CollisTopBar(
                title = {
                    Text(
                        text = "Your Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,

                        modifier = Modifier.padding(start = 10.dp)
                    )
                },
                actions = {
                    TextButton(onClick = onGoToToday) {
                        Text("Today", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SubjectsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is SubjectsUiState.Success -> {
                    SubjectsContent(
                        state = state,
                        onDateSelected = onDateSelected,
                        onPreviousWeek = onPreviousWeek,
                        onNextWeek = onNextWeek,
                        onAddTask = onNavigateToAddTask
                    )
                }

                is SubjectsUiState.Error -> {
                    ErrorState(message = state.message, onRetry = onRefresh)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SubjectsContent(
    state: SubjectsUiState.Success,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onAddTask: (String) -> Unit
) {
    var selectedSubjectDetails by remember { mutableStateOf<SubjectInfo?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            WeekNavigationHeader(
                weekStart = state.currentWeekStart,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek
            )
        }

        item {
            WeekCalendarStrip(
                weekStart = state.currentWeekStart,
                selectedDate = state.selectedDate,
                weekLessons = state.weekLessons,
                onDateSelected = onDateSelected
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                thickness = 1.dp
            )
        }

        item {
            val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM")
            Text(
                text = state.selectedDate.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state.selectedDateLessons.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.EventBusy,
                    message = "Enjoy your day! No classes scheduled.",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else {
            items(state.selectedDateLessons) { lesson ->
                ScheduleCard(
                    subjectCode = lesson.courseCode,
                    subjectName = lesson.courseTitle,
                    startTime = lesson.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = lesson.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    classroom = lesson.roomDetails,
                    lecturerName = lesson.lecturerName,
                    isLive = lesson.isLive(),
                    onClick = {
                        selectedSubjectDetails = SubjectInfo(
                            courseCode = lesson.courseCode,
                            courseTitle = lesson.courseTitle,
                            lecturerName = lesson.lecturerName,
                            totalLessons = 0 // Not needed for detail
                        )
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        if (state.allSubjects.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Subjects Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            items(state.allSubjects) { subject ->
                SubjectOverviewCard(
                    courseCode = subject.courseCode,
                    courseTitle = subject.courseTitle,
                    lecturerName = subject.lecturerName,
                    totalLessons = subject.totalLessons,
                    onClick = { selectedSubjectDetails = subject },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }

    // Subject Detail Dialog with "Add Task" button
    if (selectedSubjectDetails != null) {
        val subject = selectedSubjectDetails!!
        AlertDialog(
            onDismissRequest = { selectedSubjectDetails = null },
            title = {
                Text(
                    text = subject.courseCode,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column {
                    Text(
                        text = subject.courseTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lecturer: ${subject.lecturerName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddTask(subject.courseCode)
                        selectedSubjectDetails = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD TASK")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSubjectDetails = null }) {
                    Text("CLOSE")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekNavigationHeader(
    weekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val weekEnd = weekStart.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("MMM d")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) { Icon(Icons.Default.ChevronLeft, null) }
        Text(
            text = "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextWeek) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekCalendarStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    weekLessons: Map<LocalDate, List<LessonModel>>,
    onDateSelected: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            val lessonCount = weekLessons[date]?.size ?: 0
            DayCell(
                date = date,
                isSelected = isSelected,
                isToday = isToday,
                hasLessons = lessonCount > 0,
                lessonCount = lessonCount,
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasLessons: Boolean,
    lessonCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor
        )
        if (hasLessons) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun SubjectOverviewCard(
    courseCode: String,
    courseTitle: String,
    lecturerName: String,
    totalLessons: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(Modifier.size(48.dp), Alignment.Center) {
                    Text(
                        courseCode.take(2),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    courseCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    courseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    lecturerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun SubjectsScreenSuccessPreview() {
    val today = LocalDate.now()
    val mockLessons = listOf(
        LessonModel(
            id = 1,
            courseCode = "CS301",
            courseTitle = "Mobile App Development",
            lecturerName = "Dr. Smith",
            groupNames = listOf("Group A"),
            roomDetails = "Lab 4",
            lessonType = "Lecture",
            date = today,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            duration = 120
        ),
        LessonModel(
            id = 2,
            courseCode = "CS302",
            courseTitle = "Database Systems",
            lecturerName = "Prof. Jones",
            groupNames = listOf("Group A"),
            roomDetails = "Room 101",
            lessonType = "Lecture",
            date = today,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(16, 0),
            duration = 120
        )
    )

    val mockSubjects = listOf(
        SubjectInfo("CS301", "Mobile App Development", "Dr. Smith", 5),
        SubjectInfo("CS302", "Database Systems", "Prof. Jones", 4)
    )

    CollisTheme {
        SubjectsScreenContent(
            uiState = SubjectsUiState.Success(
                currentWeekStart = today.with(DayOfWeek.MONDAY),
                selectedDate = today,
                weekLessons = mapOf(today to mockLessons),
                selectedDateLessons = mockLessons,
                allSubjects = mockSubjects
            ),
            isRefreshing = false,
            onRefresh = {},
            onGoToToday = {},
            onDateSelected = {},
            onPreviousWeek = {},
            onNextWeek = {},
            onNavigateToAddTask = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DayCellPreview() {
    CollisTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DayCell(
                date = LocalDate.now(),
                isSelected = true,
                isToday = true,
                hasLessons = true,
                lessonCount = 2,
                onClick = {}
            )
            DayCell(
                date = LocalDate.now().plusDays(1),
                isSelected = false,
                isToday = false,
                hasLessons = false,
                lessonCount = 0,
                onClick = {}
            )
        }
    }
}
