package com.example.collis.presentation.ui.student.home


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collis.domain.model.AnnouncementModel
import com.example.collis.domain.model.LessonModel
import com.example.collis.domain.model.Task
import com.example.collis.domain.model.TaskPriority
import com.example.collis.presentation.components.*
import com.example.collis.presentation.viewmodel.MainViewModel
import com.example.collis.ui.theme.CollisTheme
import mr.dev.collis.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Student Home Screen
 *
 * IMPROVEMENTS:
 * - Integrated collis_logo in header.
 * - Added Creative Pending and Overdue status cards.
 * - Added Notification Badge.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToAnnouncement: (Int) -> Unit,
    onNavigateToSubject: (Int) -> Unit,
    onNavigateToAllTasks: () -> Unit,
    onNavigateToAllAnnouncements: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val badgeCount by mainViewModel.newNotificationCount.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDarkTheme by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        badgeCount = badgeCount,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.onEvent(HomeUiEvent.Refresh) },
        onRetry = { viewModel.onEvent(HomeUiEvent.RetryLoad) },
        onToggleTaskCompletion = { taskId ->
            viewModel.onEvent(
                HomeUiEvent.ToggleTaskCompletion(
                    taskId
                )
            )
        },
        onNavigateToTaskDetail = onNavigateToTaskDetail,
        onNavigateToAnnouncement = onNavigateToAnnouncement,
        onNavigateToSubject = onNavigateToSubject,
        onNavigateToAllTasks = onNavigateToAllTasks,
        onNavigateToAllAnnouncements = onNavigateToAllAnnouncements,
        isDarkTheme = isDarkTheme
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    isRefreshing: Boolean,
    badgeCount: Int,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onToggleTaskCompletion: (Long) -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToAnnouncement: (Int) -> Unit,
    onNavigateToSubject: (Int) -> Unit,
    onNavigateToAllTasks: () -> Unit,
    onNavigateToAllAnnouncements: () -> Unit,
    isDarkTheme: Boolean
) {
    var selectedNotification by remember { mutableStateOf<AnnouncementModel?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CollisTopBar(
                title = {
                    Image(
                        painter = painterResource(if (isDarkTheme) R.drawable.collis_light else R.drawable.collis),
                        contentDescription = null,
                        modifier = Modifier
                            .height(40.dp)
                            .width(120.dp),
                        contentScale = ContentScale.Crop
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToAllAnnouncements) {
                        BadgedBox(
                            badge = {
                                if (badgeCount > 0) {
                                    Badge {
                                        Text(text = badgeCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(CollisIcons.notificationIcon, "Notifications")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> LoadingContent()
                is HomeUiState.Success -> {
                    SuccessContent(
                        state = uiState,
                        onTaskClick = onNavigateToTaskDetail,
                        onAnnouncementClick = { announcement ->
                            selectedNotification = announcement
                        },
                        onLessonClick = { lesson -> onNavigateToSubject(lesson.id) },
                        onToggleTaskCompletion = onToggleTaskCompletion,
                        onViewAllTasks = onNavigateToAllTasks,
                        onViewAllAnnouncements = onNavigateToAllAnnouncements
                    )
                }

                is HomeUiState.Error -> {
                    ErrorState(message = uiState.message, onRetry = onRetry)
                }

                is HomeUiState.PartialSuccess -> {
                    SuccessContent(
                        state = HomeUiState.Success(
                            greeting = uiState.greeting,
                            todayLessons = uiState.todayLessons ?: emptyList(),
                            upcomingTasks = uiState.upcomingTasks ?: emptyList(),
                            recentAnnouncements = uiState.recentAnnouncements ?: emptyList(),
                            pendingTaskCount = 0,
                            overdueTaskCount = 0,
                            currentLesson = uiState.todayLessons?.firstOrNull { it.isLive() }
                        ),
                        onTaskClick = onNavigateToTaskDetail,
                        onAnnouncementClick = { announcement ->
                            selectedNotification = announcement
                        },
                        onLessonClick = { lesson -> onNavigateToSubject(lesson.id) },
                        onToggleTaskCompletion = onToggleTaskCompletion,
                        onViewAllTasks = onNavigateToAllTasks,
                        onViewAllAnnouncements = onNavigateToAllAnnouncements
                    )
                }
            }
        }
    }

    if (selectedNotification != null) {
        NotificationDetailDialog(
            notification = selectedNotification!!,
            onDismiss = { selectedNotification = null }
        )
    }
}

@Composable
private fun LoadingContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ShimmerEffect(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(32.dp)
            )
        }
        item {
            ShimmerEffect(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
        items(3) { ScheduleCardShimmer() }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SuccessContent(
    state: HomeUiState.Success,
    onTaskClick: (Long) -> Unit,
    onAnnouncementClick: (AnnouncementModel) -> Unit,
    onLessonClick: (LessonModel) -> Unit,
    onToggleTaskCompletion: (Long) -> Unit,
    onViewAllTasks: () -> Unit,
    onViewAllAnnouncements: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusCard(
                        count = state.pendingTaskCount,
                        label = "Pending",
                        icon = Icons.Default.TaskAlt,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = onViewAllTasks
                    )
                    StatusCard(
                        count = state.overdueTaskCount,
                        label = "Overdue",
                        icon = Icons.Default.ErrorOutline,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                        onClick = onViewAllTasks
                    )
                }
            }
        }

        // Improved Live Lesson Banner
        val liveLesson = state.todayLessons.find { it.isLive() } ?: state.currentLesson
        if (liveLesson != null) {
            item {
                CurrentLessonBanner(
                    lesson = liveLesson,
                    onClick = { onLessonClick(liveLesson) }
                )
            }
        }

        item {
            SectionHeader(
                title = "Today's Schedule",
                actionText = "See All",
                onActionClick = { /* Tab Nav handled by parent */ })
        }

        if (state.todayLessons.isEmpty()) {
            item { EmptyStateCard(Icons.AutoMirrored.Filled.EventNote, "Nothing scheduled today.") }
        } else {

            items(
                state.todayLessons
            ) { lesson ->

                ScheduleCard(
                    subjectCode = lesson.courseCode,
                    subjectName = lesson.courseTitle,
                    startTime = lesson.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = lesson.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    classroom = lesson.roomDetails,
                    isLive = lesson.isLive(),
                    onClick = { onLessonClick(lesson) }
                )

            }
        }
        item {
            SectionHeader(
                title = "Recent Announcements",
                actionText = "View All",
                onActionClick = onViewAllAnnouncements
            )
        }

        if (state.recentAnnouncements.isEmpty()) {
            item { EmptyStateCard(Icons.Default.Campaign, "No new updates.") }
        } else {
            items(state.recentAnnouncements) { announcement ->
                AnnouncementCard(
                    announcement = announcement,
                    onClick = { onAnnouncementClick(announcement) })
            }
        }
        item {
            SectionHeader(
                title = "Upcoming Deadlines",
                actionText = "Manage",
                onActionClick = onViewAllTasks
            )
        }

        if (state.upcomingTasks.isEmpty()) {
            item { EmptyStateCard(Icons.Default.TaskAlt, "All caught up!") }
        } else {
            items(state.upcomingTasks) { task ->
                TaskCard(
                    task = task,
                    onTaskClick = onTaskClick,
                    onToggleComplete = onToggleTaskCompletion,
                    onDeleteTask = { /* Hidden on Home */ }
                )
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

/**
 * Creative Status Card for Dashboard
 */
@Composable
private fun StatusCard(
    count: Int,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit

) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Background Decorative Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 15.dp, y = 15.dp),
                tint = contentColor.copy(alpha = 0.1f)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = contentColor
                    )
                }

                Column {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CurrentLessonBanner(
    lesson: LessonModel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inversePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            LiveIndicator()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "HAPPENING NOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    lesson.courseCode,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    lesson.courseTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Room: ${lesson.roomDetails} • Started at ${
                        lesson.startTime.format(
                            DateTimeFormatter.ofPattern(
                                "HH:mm"
                            )
                        )
                    }", style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AnnouncementCard(announcement: AnnouncementModel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val color =
                if (announcement.isUrgent()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Box(
                Modifier
                    .size(4.dp, 40.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    announcement.courseCode,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    announcement.getFormattedMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    announcement.createdAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")),
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
fun HomeScreenSuccessPreview() {
    val mockLessons = listOf(
        LessonModel(
            id = 1,
            courseCode = "CS301",
            courseTitle = "Mobile App Development",
            lecturerName = "Dr. Smith",
            groupNames = listOf("Group A"),
            roomDetails = "Lab 4",
            lessonType = "Lecture",
            date = LocalDate.now(),
            startTime = LocalTime.now().minusHours(1),
            endTime = LocalTime.now().plusHours(1),
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
            date = LocalDate.now(),
            startTime = LocalTime.now().plusHours(2),
            endTime = LocalTime.now().plusHours(4),
            duration = 120
        )
    )

    val mockTasks = listOf(
        Task(
            id = 1,
            title = "Assignment 1",
            description = "Submit before midnight",
            dueDate = LocalDateTime.now().plusDays(1),
            priority = TaskPriority.HIGH,
            isCompleted = false
        ),
        Task(
            id = 2,
            title = "Read Chapter 5",
            dueDate = LocalDateTime.now().plusDays(3),
            priority = TaskPriority.MEDIUM,
            isCompleted = false
        )
    )

    val mockAnnouncements = listOf(
        AnnouncementModel(
            id = 1,
            courseCode = "CS301",
            courseTitle = "Mobile App Development",
            lessonDate = "2026-02-23",
            lessonTime = "10:00:00",
            groupNames = listOf("Group A"),
            messageType = "NORMAL",
            messageTypeDisplay = "Normal",
            messageText = "Check Moodle",
            createdAt = LocalDateTime.now().minusHours(2)
        )
    )

    CollisTheme {
        HomeScreenContent(
            uiState = HomeUiState.Success(
                greeting = "Good Morning, Student!",
                todayLessons = mockLessons,
                upcomingTasks = mockTasks,
                recentAnnouncements = mockAnnouncements,
                pendingTaskCount = 5,
                overdueTaskCount = 2,
                currentLesson = mockLessons[0]
            ),
            isRefreshing = false,
            badgeCount = 3,
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onRetry = {},
            onToggleTaskCompletion = {},
            onNavigateToTaskDetail = {},
            onNavigateToAnnouncement = {},
            onNavigateToSubject = {},
            onNavigateToAllTasks = {},
            onNavigateToAllAnnouncements = {},
            false
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenLoadingPreview() {
    CollisTheme {
        HomeScreenContent(
            uiState = HomeUiState.Loading,
            isRefreshing = false,
            badgeCount = 0,
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onRetry = {},
            onToggleTaskCompletion = {},
            onNavigateToTaskDetail = {},
            onNavigateToAnnouncement = {},
            onNavigateToSubject = {},
            onNavigateToAllTasks = {},
            onNavigateToAllAnnouncements = {},
            false
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AnnouncementCardPreview() {
    val mockAnnouncement = AnnouncementModel(
        id = 1,
        courseCode = "CS301",
        courseTitle = "Mobile App Development",
        lessonDate = "2026-02-23",
        lessonTime = "10:00:00",
        groupNames = listOf("Group A"),
        messageType = "URGENT",
        messageTypeDisplay = "Urgent",
        messageText = "Cancelled",
        createdAt = LocalDateTime.now().minusHours(1)
    )
    CollisTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AnnouncementCard(announcement = mockAnnouncement, onClick = {})
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun StatusCardPreview() {
    CollisTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(
                count = 5,
                label = "Pending",
                icon = Icons.Default.TaskAlt,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }
    }
}
