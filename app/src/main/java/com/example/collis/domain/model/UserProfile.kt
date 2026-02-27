package com.example.collis.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.data.remote.dto.toLocalDate
import com.example.collis.data.remote.dto.toLocalTime
import com.example.collis.data.remote.dto.toUiFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * User Profile Domain Model
 */
data class UserProfile(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val userType: String,  // "STUDENT"
    val userTypeDisplay: String,  // "Student"
    val groupName: String?  // Student group name
) {
    fun isStudent(): Boolean = userType == "STUDENT"
    fun isLecturer(): Boolean = userType == "LECTURER"
    fun getDisplayName(): String = fullName
    fun getInitials(): String = fullName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    fun isComplete(): Boolean = username.isNotBlank() && fullName.isNotBlank() && email.isNotBlank()
}

/**
 * Lesson Domain Model
 */
data class LessonModel(
    val id: Int,
    val courseCode: String,
    val courseTitle: String,
    val lecturerName: String,
    val groupNames: List<String>,
    val roomDetails: String,
    val lessonType: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val duration: Int
) {
    /**
     * Check if lesson is happening now
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isLive(): Boolean {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val currentTime = now.toLocalTime()

        // Fix: Use inclusive bounds for start time
        return date == today &&
                (currentTime.isAfter(startTime) || currentTime == startTime) &&
                currentTime.isBefore(endTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isToday(): Boolean = date == LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getStatus(): LessonStatus {
        val now = LocalDateTime.now()
        val lessonStart = LocalDateTime.of(date, startTime)
        val lessonEnd = LocalDateTime.of(date, endTime)

        return when {
            now.isBefore(lessonStart) -> LessonStatus.UPCOMING
            now.isAfter(lessonEnd) -> LessonStatus.COMPLETED
            else -> LessonStatus.LIVE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeRange(): String {
        return "${startTime.toUiFormat()} - ${endTime.toUiFormat()}"
    }
}

enum class LessonStatus {
    UPCOMING,
    LIVE,
    COMPLETED,
    CANCELLED
}

/**
 * Notification/Announcement Model
 */
data class AnnouncementModel(
    val id: Int,
    val courseCode: String,
    val courseTitle: String,
    val lessonDate: String, // "yyyy-MM-dd" from backend
    val lessonTime: String, // "HH:mm:ss" from backend
    val groupNames: List<String>,
    val messageType: String,
    val messageTypeDisplay: String,
    val messageText: String,
    val createdAt: LocalDateTime
) {
    fun isUrgent(): Boolean = messageType == "CANCELLATION" || messageType == "RESCHEDULE"
    
    fun getPriority(): AnnouncementPriority {
        return when (messageType) {
            "CANCELLATION" -> AnnouncementPriority.URGENT
            "RESCHEDULE" -> AnnouncementPriority.HIGH
            else -> AnnouncementPriority.NORMAL
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDate(): String = lessonDate.toLocalDate()?.toUiFormat() ?: lessonDate

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedTime(): String = lessonTime.toLocalTime()?.toUiFormat() ?: lessonTime

    /**
     * Generates a clear, user-friendly message based on the notification type.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedMessage(): String {
        val date = getFormattedDate()
        val time = getFormattedTime()
        
        return when (messageType.uppercase()) {
            "CANCELLATION" -> "Lesson on $date cancelled."
            "RESCHEDULE" -> "Lesson rescheduled to $date at $time."
            "ANNOUNCEMENT" -> {
                if (messageText.contains("added", ignoreCase = true)) {
                    "Lesson added to $date."
                } else {
                    messageText
                }
            }
            else -> messageText
        }
    }
}

enum class AnnouncementPriority {
    NORMAL,
    HIGH,
    URGENT
}