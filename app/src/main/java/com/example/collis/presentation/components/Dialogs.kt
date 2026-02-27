package com.example.collis.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collis.domain.model.AnnouncementModel
import com.example.collis.ui.theme.CollisTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Professional Notification Detail Dialog
 * Provides clear information about schedule changes or announcements.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationDetailDialog(
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GOT IT")
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = color
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = notification.messageTypeDisplay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Subject Information
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.courseCode,
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = notification.courseTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Announcement Text
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = notification.messageText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }

                // Schedule Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        icon = Icons.Default.Event,
                        label = "Lesson Date",
                        value = notification.lessonDate
                    )
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Lesson Time",
                        value = notification.lessonTime
                    )
                    
                    if (notification.groupNames.isNotEmpty()) {
                        DetailRow(
                            icon = Icons.Default.Groups,
                            label = "Recipient Groups",
                            value = notification.groupNames.joinToString(", ")
                        )
                    }
                    
                    DetailRow(
                        icon = Icons.Default.AccessTime,
                        label = "Received At",
                        value = notification.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun NotificationDetailDialogPreview() {
    val mockNotification = AnnouncementModel(
        id = 1,
        courseCode = "CS301",
        courseTitle = "Mobile App Development",
        lessonDate = "Monday, 19 May",
        lessonTime = "10:00 AM - 12:00 PM",
        groupNames = listOf("Group A", "Group B"),
        messageType = "CANCELLATION",
        messageTypeDisplay = "Cancellation",
        messageText = "The lecture today is cancelled due to technical issues in Lab 4. Please refer to Moodle for online materials.",
        createdAt = LocalDateTime.now()
    )
    CollisTheme {
        NotificationDetailDialog(notification = mockNotification, onDismiss = {})
    }
}
