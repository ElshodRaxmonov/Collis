package com.example.collis.data.repository


import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.data.network.NetworkResult
import com.example.collis.data.remote.api.CollisApiService
import com.example.collis.data.remote.dto.*
import com.example.collis.domain.model.LessonModel
import com.example.collis.domain.repository.LessonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lesson Repository Implementation
 *
 * CACHING STRATEGY:
 * - No local caching for now (always fetch from server)
 * - Future: Add Room database for offline support
 *
 * ERROR HANDLING:
 * - Network errors
 * - Permission errors (lecturer trying to modify others' lessons)
 * - Validation errors (invalid dates, times)
 */
@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val apiService: CollisApiService,
    private val preferencesManager: PreferencesManager
) : LessonRepository {

    /**
     * Get today's lessons
     *
     * IMPLEMENTATION:
     * 1. Get current user type
     * 2. If student: fetch by group
     * 3. If lecturer: fetch by lecturer ID
     * 4. Filter for today's date
     * 5. Map to domain models
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getTodayLessons(): Flow<NetworkResult<List<LessonModel>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val today = LocalDate.now().toString()

            /**
             * Fetch lessons for today
             * API filters by date parameter
             */
            val response = apiService.getLessons(
                date = today,
                pageSize = 100  // Get all lessons for today
            )

            if (response.isSuccessful) {
                val lessons = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()

                /**
                 * Sort by start time
                 * Shows earliest lessons first
                 */
                val sortedLessons = lessons.sortedBy { it.startTime }

                emit(NetworkResult.Success(sortedLessons))
            } else {
                emit(NetworkResult.Error("Failed to load today's schedule"))
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error"))
        }
    }

    /**
     * Get lessons for specific date
     */
    override fun getLessonsForDate(date: LocalDate): Flow<NetworkResult<List<LessonModel>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getLessons(
                date = date.toString(),
                pageSize = 100
            )

            if (response.isSuccessful) {
                val lessons = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()

                emit(NetworkResult.Success(lessons.sortedBy { it.startTime }))
            } else {
                emit(NetworkResult.Error("Failed to load lessons"))
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error"))
        }
    }

    /**
     * Get lessons for date range
     *
     * USAGE: Weekly view (Monday to Sunday)
     */
    override fun getLessonsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<NetworkResult<List<LessonModel>>> = flow {
        emit(NetworkResult.Loading())

        try {
            /**
             * API supports date range filtering
             * date__gte: Greater than or equal (start)
             * date__lte: Less than or equal (end)
             */
            val response = apiService.getLessons(
                dateFrom = startDate.toString(),
                dateTo = endDate.toString(),
                pageSize = 500  // Assume max 500 lessons per week
            )

            if (response.isSuccessful) {
                val lessons = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()

                /**
                 * Sort by date, then time
                 * Shows chronological order
                 */
                val sortedLessons = lessons.sortedWith(
                    compareBy({ it.date }, { it.startTime })
                )

                emit(NetworkResult.Success(sortedLessons))
            } else {
                emit(NetworkResult.Error("Failed to load lessons"))
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error"))
        }
    }

    /**
     * Get all lessons (paginated)
     *
     * NON-REACTIVE: Returns one-time result
     * Use for initial load or pagination
     */
    override suspend fun getAllLessons(
        page: Int,
        pageSize: Int
    ): NetworkResult<List<LessonModel>> {
        return try {
            val response = apiService.getLessons(
                page = page,
                pageSize = pageSize
            )

            if (response.isSuccessful) {
                val lessons = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()

                NetworkResult.Success(lessons)
            } else {
                NetworkResult.Error("Failed to load lessons")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Get lesson by ID
     */
    override suspend fun getLessonById(lessonId: Int): NetworkResult<LessonModel> {
        return try {
            val response = apiService.getLessonById(lessonId)

            if (response.isSuccessful) {
                val lesson = response.body()?.toDomainModel()
                    ?: return NetworkResult.Error("Lesson not found")

                NetworkResult.Success(lesson)
            } else {
                NetworkResult.Error("Failed to load lesson details")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Get lessons by course
     */
    override fun getLessonsByCourse(courseId: Int): Flow<NetworkResult<List<LessonModel>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getLessons(
                courseId = courseId,
                pageSize = 100
            )

            if (response.isSuccessful) {
                val lessons = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()

                emit(NetworkResult.Success(lessons))
            } else {
                emit(NetworkResult.Error("Failed to load course lessons"))
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error"))
        }
    }

    /**
     * Get lessons by lecturer
     */
    override fun getLessonsByLecturer(lecturerId: String): Flow<NetworkResult<List<LessonModel>>> =
        flow {
            emit(NetworkResult.Loading())

            try {
                val response = apiService.getLessons(
                    lecturerId = lecturerId,
                    pageSize = 100
                )

                if (response.isSuccessful) {
                    val lessons = response.body()?.results?.map { it.toDomainModel() }
                        ?: emptyList()

                    emit(NetworkResult.Success(lessons))
                } else {
                    emit(NetworkResult.Error("Failed to load lecturer schedule"))
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(NetworkResult.Error(e.message ?: "Network error"))
            }
        }

    /**
     * Get lessons by group
     */
    override fun getLessonsByGroup(groupId: Int): Flow<NetworkResult<List<LessonModel>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getLessons(
                groupId = groupId,
                pageSize = 100
            )

            if (response.isSuccessful) {
                val lessons = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()

                emit(NetworkResult.Success(lessons))
            } else {
                emit(NetworkResult.Error("Failed to load group schedule"))
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error"))
        }
    }

    /**
     * Create lesson (Lecturer only)
     *
     * WORKFLOW:
     * 1. Validate inputs
     * 2. Create request DTO
     * 3. Call API
     * 4. Return success/error
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createLesson(
        courseId: Int,
        lecturerId: String,
        groupIds: List<Int>,
        roomId: Int,
        lessonType: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): NetworkResult<LessonModel> {
        return try {
            /**
             * Validate inputs
             */
            if (date.isBefore(LocalDate.now())) {
                return NetworkResult.Error("Cannot create lesson in the past")
            }

            if (startTime.isAfter(endTime) || startTime == endTime) {
                return NetworkResult.Error("Invalid time range")
            }

            if (groupIds.isEmpty()) {
                return NetworkResult.Error("At least one group must be selected")
            }

            /**
             * Create request
             * Convert LocalTime to "HH:MM:SS" format
             */
            val request = LessonCreateRequest(
                courseId = courseId,
                lecturerId = lecturerId,
                groupIds = groupIds,
                roomId = roomId,
                lessonType = lessonType,
                date = date.toString(),
                startTime = startTime.toString(),
                endTime = endTime.toString()
            )

            /**
             * Call API
             */
            val response = apiService.createLesson(request)

            if (response.isSuccessful) {
                val lesson = response.body()?.toDomainModel()
                    ?: return NetworkResult.Error("Failed to parse response")

                NetworkResult.Success(lesson)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid lesson data"
                    403 -> "You don't have permission to create lessons"
                    409 -> "Scheduling conflict detected"
                    else -> "Failed to create lesson"
                }
                NetworkResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Update lesson (Lecturer only)
     *
     * IMPLEMENTATION:
     * - Only include fields that changed
     * - Use PATCH for partial update
     * - Triggers notification to students
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateLesson(
        lessonId: Int,
        courseId: Int?,
        groupIds: List<Int>?,
        roomId: Int?,
        lessonType: String?,
        date: LocalDate?,
        startTime: LocalTime?,
        endTime: LocalTime?
    ): NetworkResult<LessonModel> {
        return try {
            /**
             * Build partial update map
             * Only include non-null fields
             */
            val updateMap = mutableMapOf<String, Any>()

            courseId?.let { updateMap["course"] = it }
            groupIds?.let { updateMap["groups"] = it }
            roomId?.let { updateMap["room"] = it }
            lessonType?.let { updateMap["lesson_type"] = it }
            date?.let { updateMap["date"] = it.toString() }
            startTime?.let { updateMap["starting_time"] = it.toString() }
            endTime?.let { updateMap["ending_time"] = it.toString() }

            if (updateMap.isEmpty()) {
                return NetworkResult.Error("No changes to update")
            }

            /**
             * Validate time range if both provided
             */
            if (startTime != null && endTime != null) {
                if (startTime.isAfter(endTime) || startTime == endTime) {
                    return NetworkResult.Error("Invalid time range")
                }
            }

            /**
             * Call API with partial update
             */
            val response = apiService.patchLesson(lessonId, updateMap)

            if (response.isSuccessful) {
                val lesson = response.body()?.toDomainModel()
                    ?: return NetworkResult.Error("Failed to parse response")

                NetworkResult.Success(lesson)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid lesson data"
                    403 -> "You don't have permission to update this lesson"
                    404 -> "Lesson not found"
                    409 -> "Scheduling conflict detected"
                    else -> "Failed to update lesson"
                }
                NetworkResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Delete lesson (Lecturer only)
     *
     * SOFT DELETE:
     * - Lesson marked as deleted, not removed
     * - Creates cancellation notification
     * - Students see "Cancelled" status
     */
    override suspend fun deleteLesson(lessonId: Int): NetworkResult<Unit> {
        return try {
            val response = apiService.deleteLesson(lessonId)

            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    403 -> "You don't have permission to delete this lesson"
                    404 -> "Lesson not found"
                    else -> "Failed to delete lesson"
                }
                NetworkResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Refresh lessons
     *
     * USAGE: Pull to refresh gesture
     * Forces fresh data from server
     */
    override suspend fun refreshLessons(): NetworkResult<Unit> {
        return try {
            // Simply fetch latest lessons
            // In real implementation, might clear cache first
            val response = apiService.getLessons(page = 1, pageSize = 1)

            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Failed to refresh")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}

/**
 * DTO to Domain Model Mapper
 *
 * MAPPING PATTERN:
 * - Extension function on DTO
 * - Converts API response to domain model
 * - Handles parsing (strings to dates/times)
 * - Provides defaults for nullable fields
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun Lesson.toDomainModel(): LessonModel {
    return LessonModel(
        id = this.id,
        courseCode = this.courseCode,
        courseTitle = this.courseTitle,
        lecturerName = this.lecturerName,
        groupNames = this.groupNames,  // Already List<String> from backend
        roomDetails = this.roomDetails,
        lessonType = this.lessonType,
        date = this.date.toLocalDate() ?: LocalDate.now(),
        startTime = this.startTime.toLocalTime() ?: LocalTime.MIN,
        endTime = this.endTime.toLocalTime() ?: LocalTime.MAX,
        duration = this.duration  // Already Int from backend
    )
}