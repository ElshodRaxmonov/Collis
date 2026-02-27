package com.example.collis.presentation.ui.student.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collis.domain.model.AnnouncementModel
import com.example.collis.presentation.components.*
import com.example.collis.ui.theme.CollisTheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    NotificationScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        selectedFilter = selectedFilter,
        onRefresh = { viewModel.refresh() },
        onSetFilter = { viewModel.setFilter(it) },
        onNavigateBack = onNavigateBack
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreenContent(
    uiState: NotificationUiState,
    isRefreshing: Boolean,
    selectedFilter: NotificationFilter,
    onRefresh: () -> Unit,
    onSetFilter: (NotificationFilter) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedNotification by remember { mutableStateOf<AnnouncementModel?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CollisTopBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(NotificationFilter.entries) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onSetFilter(filter) },
                            label = { Text(filter.displayName) },
                            leadingIcon = if (selectedFilter == filter) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                when (val state = uiState) {
                    is NotificationUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is NotificationUiState.Success -> {
                        if (state.notifications.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.NotificationsNone,
                                title = "No notifications",
                                message = "You're all caught up! Check back later for updates."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.notifications) { notification ->
                                    NotificationCard(
                                        notification = notification,
                                        onClick = { selectedNotification = notification }
                                    )
                                }
                            }
                        }
                    }

                    is NotificationUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = onRefresh
                        )
                    }
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NotificationCard(
    notification: AnnouncementModel,
    onClick: () -> Unit
) {
    val icon = when (notification.messageType) {
        "CANCELLATION" -> Icons.Default.Cancel
        "RESCHEDULE" -> Icons.Default.Schedule
        else -> Icons.Default.Info
    }

    val iconColor = when (notification.messageType) {
        "CANCELLATION" -> MaterialTheme.colorScheme.error
        "RESCHEDULE" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomEnd = 36.dp,
            bottomStart = 36.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = notification.messageTypeDisplay,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = iconColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = notification.messageTypeDisplay.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${notification.courseCode} - ${notification.courseTitle}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.getFormattedMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lesson info
                    Text(
                        text = "${notification.getFormattedDate()} • ${notification.getFormattedTime()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    // Relative time
                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationDetailDialog(
    notification: AnnouncementModel,
    onDismiss: () -> Unit
) {
    val icon = when (notification.messageType) {
        "CANCELLATION" -> Icons.Default.Cancel
        "RESCHEDULE" -> Icons.Default.Schedule
        "ROOM_CHANGE" -> Icons.Default.Room
        else -> Icons.Default.Info
    }

    val color = when (notification.messageType) {
        "CANCELLATION" -> MaterialTheme.colorScheme.error
        "RESCHEDULE" -> MaterialTheme.colorScheme.tertiary
        "ROOM_CHANGE" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GOT IT")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = notification.messageTypeDisplay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header with subject
                Column {
                    Text(
                        text = notification.courseCode,
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = notification.courseTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Main Message
                Surface(
                    color = color.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        color.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = notification.getFormattedMessage(),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }

                // Details Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailItem(Icons.Default.Event, "Lesson Date", notification.getFormattedDate())
                    DetailItem(
                        Icons.Default.Schedule,
                        "Lesson Time",
                        notification.getFormattedTime()
                    )
                    if (notification.groupNames.isNotEmpty()) {
                        DetailItem(
                            Icons.Default.Groups,
                            "Target Groups",
                            notification.groupNames.joinToString(", ")
                        )
                    }
                    DetailItem(
                        Icons.Default.AccessTime,
                        "Received At",
                        notification.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatRelativeTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(dateTime, now)

    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
        duration.toHours() < 24 -> "${duration.toHours()}h ago"
        duration.toDays() == 1L -> "Yesterday"
        duration.toDays() < 7 -> "${duration.toDays()}d ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun NotificationScreenSuccessPreview() {
    val mockNotifications = listOf(
        AnnouncementModel(
            id = 1,
            courseCode = "CS301",
            courseTitle = "Mobile App Development",
            lessonDate = "2026-02-23",
            lessonTime = "10:00:00",
            groupNames = listOf("Group A", "Group B"),
            messageType = "CANCELLATION",
            messageTypeDisplay = "Cancellation",
            messageText = "Technical issues",
            createdAt = LocalDateTime.now().minusHours(1)
        ),
        AnnouncementModel(
            id = 2,
            courseCode = "CS302",
            courseTitle = "Database Systems",
            lessonDate = "2026-02-24",
            lessonTime = "14:00:00",
            groupNames = listOf("Group A"),
            messageType = "RESCHEDULE",
            messageTypeDisplay = "Reschedule",
            messageText = "Room change",
            createdAt = LocalDateTime.now().minusDays(1)
        )
    )

    CollisTheme {
        NotificationScreenContent(
            uiState = NotificationUiState.Success(mockNotifications),
            isRefreshing = false,
            selectedFilter = NotificationFilter.ALL,
            onRefresh = {},
            onSetFilter = {},
            onNavigateBack = {}
        )
    }
}
