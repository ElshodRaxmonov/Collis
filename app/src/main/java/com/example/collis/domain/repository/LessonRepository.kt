package com.example.collis.domain.repository



import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.LessonModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Lesson Repository Interface
 *
 * RESPONSIBILITIES:
 * - Fetch lessons (today, week, filtered)
 * - Create/Update/Delete lessons (lecturer only)
 * - Manage lesson schedule
 *
 * STUDENT USE CASES:
 * - View today's schedule
 * - View weekly schedule
 * - View lessons by subject
 *
 * LECTURER USE CASES:
 * - Create new lessons
 * - Reschedule lessons
 * - Cancel lessons
 * - View teaching schedule
 */
interface LessonRepository {

    /**
     * Get today's lessons for current user
     *
     * STUDENT: Returns lessons for their group
     * LECTURER: Returns lessons they teach today
     *
     * @return Flow of lessons (reactive, updates automatically)
     */
    fun getTodayLessons(): Flow<NetworkResult<List<LessonModel>>>

    /**
     * Get lessons for specific date
     *
     * @param date Date to fetch lessons for
     * @return Flow of lessons for that date
     */
    fun getLessonsForDate(date: LocalDate): Flow<NetworkResult<List<LessonModel>>>

    /**
     * Get lessons for date range
     *
     * USAGE: Weekly view, monthly calendar
     *
     * @param startDate Start of range (inclusive)
     * @param endDate End of range (inclusive)
     * @return Flow of lessons in date range
     */
    fun getLessonsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<NetworkResult<List<LessonModel>>>

    /**
     * Get all lessons (paginated)
     *
     * @param page Page number (1-indexed)
     * @param pageSize Items per page
     * @return One-time result (not reactive)
     */
    suspend fun getAllLessons(
        page: Int = 1,
        pageSize: Int = 20
    ): NetworkResult<List<LessonModel>>

    /**
     * Get lesson by ID
     *
     * @param lessonId Lesson ID
     * @return Lesson details
     */
    suspend fun getLessonById(lessonId: Int): NetworkResult<LessonModel>

    /**
     * Get lessons by course
     *
     * USAGE: View all sessions of a specific course
     *
     * @param courseId Course ID
     * @return Flow of lessons for that course
     */
    fun getLessonsByCourse(courseId: Int): Flow<NetworkResult<List<LessonModel>>>

    /**
     * Get lessons by lecturer
     *
     * USAGE: View all lessons taught by specific lecturer
     *
     * @param lecturerId Lecturer ID
     * @return Flow of lessons taught by that lecturer
     */
    fun getLessonsByLecturer(lecturerId: String): Flow<NetworkResult<List<LessonModel>>>

    /**
     * Get lessons by group
     *
     * USAGE: View schedule for specific student group
     *
     * @param groupId Group ID
     * @return Flow of lessons for that group
     */
    fun getLessonsByGroup(groupId: Int): Flow<NetworkResult<List<LessonModel>>>

    /**
     * Create new lesson (Lecturer only)
     *
     * VALIDATION:
     * - Date must be in future
     * - Start time before end time
     * - Room available
     * - No scheduling conflicts
     *
     * @param courseId Course to teach
     * @param lecturerId Lecturer teaching
     * @param groupIds Which groups attend
     * @param roomId Classroom
     * @param lessonType "Lecture", "Lab", "Tutorial"
     * @param date Lesson date
     * @param startTime Start time
     * @param endTime End time
     * @return Created lesson or error
     */
    suspend fun createLesson(
        courseId: Int,
        lecturerId: String,
        groupIds: List<Int>,
        roomId: Int,
        lessonType: String,
        date: LocalDate,
        startTime: java.time.LocalTime,
        endTime: java.time.LocalTime
    ): NetworkResult<LessonModel>

    /**
     * Update lesson (Lecturer only)
     *
     * TRIGGERS: Notification to students about change
     *
     * @param lessonId Lesson to update
     * @param courseId Updated course (optional)
     * @param groupIds Updated groups (optional)
     * @param roomId Updated room (optional)
     * @param lessonType Updated type (optional)
     * @param date Updated date (optional)
     * @param startTime Updated start time (optional)
     * @param endTime Updated end time (optional)
     * @return Updated lesson or error
     */
    suspend fun updateLesson(
        lessonId: Int,
        courseId: Int? = null,
        groupIds: List<Int>? = null,
        roomId: Int? = null,
        lessonType: String? = null,
        date: LocalDate? = null,
        startTime: java.time.LocalTime? = null,
        endTime: java.time.LocalTime? = null
    ): NetworkResult<LessonModel>

    /**
     * Delete/Cancel lesson (Lecturer only)
     *
     * TRIGGERS: Cancellation notification to students
     *
     * @param lessonId Lesson to cancel
     * @return Success or error
     */
    suspend fun deleteLesson(lessonId: Int): NetworkResult<Unit>

    /**
     * Refresh lessons from server
     *
     * USAGE: Pull to refresh
     */
    suspend fun refreshLessons(): NetworkResult<Unit>
}