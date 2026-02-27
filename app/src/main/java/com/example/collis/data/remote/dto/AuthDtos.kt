package com.example.collis.data.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DTOs matching your PythonAnywhere Swagger API
 */

// ==================== AUTHENTICATION ====================

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("user_type")
    val userType: String? = null,
    @SerializedName("user_type_display")
    val userTypeDisplay: String? = null,
    @SerializedName("fullname")
    val fullname: String? = null,
    @SerializedName("student_id")
    val studentId: String? = null,
    @SerializedName("lecturer_id")
    val lecturerId: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class MessageResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("success")
    val success: Boolean = true
)

// ==================== PROFILE ====================

data class Profile(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("user_type")
    val userType: String,
    @SerializedName("user_type_display")
    val userTypeDisplay: String,
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("fullname")
    val fullName: String,
    @SerializedName("group_name")
    val groupName: String?
)

data class Student(
    @SerializedName("student_id")
    val studentId: String,
    @SerializedName("fullname")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("group")
    val groupId: Int,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class LecturerProfile(
    @SerializedName("lecturer_id")
    val lecturerId: String,
    @SerializedName("fullname")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// ==================== COURSES ====================

data class Course(
    @SerializedName("course_code")
    val courseCode: String,
    @SerializedName("title")
    val courseTitle: String,
    @SerializedName("credits")
    val credits: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// ==================== GROUPS ====================

data class Group(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("intake")
    val intake: String,
    @SerializedName("student_count")
    val studentCount: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// ==================== ROOMS ====================

data class Room(
    @SerializedName("id")
    val id: Int,
    @SerializedName("building")
    val building: String,
    @SerializedName("hall")
    val hall: String,
    @SerializedName("capacity")
    val capacity: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// ==================== LESSONS (Schedule) ====================

data class Lesson(
    @SerializedName("id")
    val id: Int,
    @SerializedName("course")
    val courseId: Int,
    @SerializedName("course_code")
    val courseCode: String,
    @SerializedName("course_title")
    val courseTitle: String,
    @SerializedName("lecturer")
    val lecturerId: String,
    @SerializedName("lecturer_id")
    val lecturerIdDuplicate: String,
    @SerializedName("lecturer_name")
    val lecturerName: String,
    @SerializedName("groups")
    val groupIds: List<Int>,
    @SerializedName("group_names")
    val groupNames: List<String>,
    @SerializedName("room")
    val roomId: Int,
    @SerializedName("room_details")
    val roomDetails: String,
    @SerializedName("lesson_type")
    val lessonType: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("starting_time")
    val startTime: String,
    @SerializedName("ending_time")
    val endTime: String,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class LessonCreateRequest(
    @SerializedName("course")
    val courseId: Int,
    @SerializedName("lecturer")
    val lecturerId: String,
    @SerializedName("groups")
    val groupIds: List<Int>,
    @SerializedName("room")
    val roomId: Int,
    @SerializedName("lesson_type")
    val lessonType: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("starting_time")
    val startTime: String,
    @SerializedName("ending_time")
    val endTime: String
)

typealias LessonUpdateRequest = LessonCreateRequest

data class LessonDetails(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("course_code")
    val courseCode: String? = null,
    @SerializedName("course_title")
    val courseTitle: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("room")
    val room: String? = null,
    @SerializedName("lecturer")
    val lecturer: String? = null,
    @SerializedName("deleted")
    val deleted: Boolean? = null
)

// ==================== NOTIFICATIONS ====================

data class Notification(
    @SerializedName("id")
    val id: Int,
    @SerializedName("lesson")
    val lessonId: Int?,
    @SerializedName("lesson_details")
    val lessonDetails: LessonDetails?,
    @SerializedName("course_code")
    val courseCode: String,
    @SerializedName("course_title")
    val courseTitle: String,
    @SerializedName("lesson_date")
    val lessonDate: String,
    @SerializedName("lesson_time")
    val lessonTime: String,
    @SerializedName("group_names")
    val groupNames: String,
    @SerializedName("message_type")
    val messageType: String,
    @SerializedName("message_type_display")
    val messageTypeDisplay: String,
    @SerializedName("message_text")
    val messageText: String,
    @SerializedName("is_sent")
    val isSent: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

// ==================== PAGINATION ====================

data class PaginatedResponse<T>(
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("results")
    val results: List<T>
)

// ==================== HELPER EXTENSIONS ====================

@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDate(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalTime(): LocalTime? {
    return try {
        LocalTime.parse(this)
    } catch (e: Exception) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDateTime(): LocalDateTime? {
    return try {
        // Handle ISO-8601 with and without milliseconds/Z
        val clean = this.replace("Z", "").substringBefore("+")
        if (clean.contains("T")) {
            LocalDateTime.parse(clean)
        } else {
            // Handle space instead of T (some APIs do this)
            val parts = clean.split(" ")
            if (parts.size == 2) {
                LocalDateTime.of(LocalDate.parse(parts[0]), LocalTime.parse(parts[1]))
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Formatters for consistent UI display across the app.
 */
object CollisFormatters {
    @RequiresApi(Build.VERSION_CODES.O)
    val uiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM", Locale.ENGLISH)
    
    @RequiresApi(Build.VERSION_CODES.O)
    val uiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    
    @RequiresApi(Build.VERSION_CODES.O)
    val apiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    @RequiresApi(Build.VERSION_CODES.O)
    val apiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toUiFormat(): String = this.format(CollisFormatters.uiDateFormatter)

@RequiresApi(Build.VERSION_CODES.O)
fun LocalTime.toUiFormat(): String = this.format(CollisFormatters.uiTimeFormatter).lowercase()
