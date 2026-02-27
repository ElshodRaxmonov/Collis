package com.example.collis.data.remote.api


import com.example.collis.data.remote.dto.ChangePasswordRequest
import com.example.collis.data.remote.dto.Course
import com.example.collis.data.remote.dto.Group
import com.example.collis.data.remote.dto.LecturerProfile
import com.example.collis.data.remote.dto.Lesson
import com.example.collis.data.remote.dto.LessonCreateRequest
import com.example.collis.data.remote.dto.LessonUpdateRequest
import com.example.collis.data.remote.dto.LoginRequest
import com.example.collis.data.remote.dto.LoginResponse
import com.example.collis.data.remote.dto.MessageResponse
import com.example.collis.data.remote.dto.Notification
import com.example.collis.data.remote.dto.PaginatedResponse
import com.example.collis.data.remote.dto.Profile
import com.example.collis.data.remote.dto.Room
import com.example.collis.data.remote.dto.Student
import retrofit2.Response
import retrofit2.http.*

/**
 * Collis API Service - PythonAnywhere Backend
 *
 * BASE URL: https://collis.pythonanywhere.com/api/
 * AUTHENTICATION: DRF Token Authentication (Authorization: Token <key>)
 * DOCUMENTATION: /api/schema/swagger-ui/
 *
 * CONVENTIONS:
 * - All endpoints return Response<T> for error handling
 * - suspend functions for coroutine support
 * - @Path for URL parameters
 * - @Query for query parameters
 * - @Body for request bodies
 *
 * LEARNING: Retrofit generates implementation automatically
 * Each method becomes actual HTTP call at runtime
 */
interface CollisApiService {

    // ==================== AUTHENTICATION ====================

    /**
     * Login - Get JWT Tokens
     * POST /api/token/
     *
     * NO AUTHENTICATION REQUIRED (public endpoint)
     *
     * Returns access and refresh JWT tokens
     * Access token: Short-lived (~1 hour)
     * Refresh token: Long-lived (~7 days)
     *
     * USAGE:
     * 1. User enters username/password
     * 2. Call this endpoint
     * 3. Store tokens securely (DataStore)
     * 4. Use access token for authenticated requests
     */
    @POST("token/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>


    suspend fun logout(): Response<Unit>

    // Note: Backend uses Token auth (not JWT), so no refresh endpoint exists.
    // On 401, user must re-login.

    // ==================== PROFILE ====================

    /**
     * Get Current User Profile
     * GET /api/profiles/me/
     *
     * REQUIRES: Authentication
     *
     * Returns profile of currently logged-in user
     * Works for both students and lecturers
     * Check user_type field to determine role
     */
    @GET("profiles/me/")
    suspend fun getMyProfile(): Response<Profile>

    /**
     * Get All Profiles
     * GET /api/profiles/
     *
     * REQUIRES: Authentication (likely admin only)
     * PAGINATION: Yes
     *
     * Returns paginated list of all user profiles
     */
    @GET("profiles/")
    suspend fun getProfiles(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Profile>>

    /**
     * Get Profile by ID
     * GET /api/profiles/{id}/
     *
     * REQUIRES: Authentication
     */
    @GET("profiles/{id}/")
    suspend fun getProfileById(
        @Path("id") profileId: String
    ): Response<Profile>

    /**
     * Change Password
     * POST /api/profiles/change_password/
     *
     * REQUIRES: Authentication
     *
     * User must provide old password for verification
     */
    @POST("profiles/change_password/")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>

    // ==================== STUDENTS ====================

    /**
     * Get All Students
     * GET /api/students/
     *
     * REQUIRES: Authentication (likely lecturer/admin only)
     * PAGINATION: Yes
     */
    @GET("students/")
    suspend fun getStudents(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Student>>

    /**
     * Get Student by ID
     * GET /api/students/{student_id}/
     *
     * REQUIRES: Authentication
     */
    @GET("students/{student_id}/")
    suspend fun getStudentById(
        @Path("student_id") studentId: String
    ): Response<Student>

    // ==================== LECTURERS ====================

    /**
     * Get All Lecturers
     * GET /api/lecturers/
     *
     * REQUIRES: Authentication
     * PAGINATION: Yes
     */
    @GET("lecturers/")
    suspend fun getLecturers(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<LecturerProfile>>

    /**
     * Get Lecturer by ID
     * GET /api/lecturers/{lecturer_id}/
     *
     * REQUIRES: Authentication
     */
    @GET("lecturers/{lecturer_id}/")
    suspend fun getLecturerById(
        @Path("lecturer_id") lecturerId: String
    ): Response<LecturerProfile>

    // ==================== COURSES ====================

    /**
     * Get All Courses
     * GET /api/courses/
     *
     * REQUIRES: Authentication
     * PAGINATION: Yes
     *
     * Returns list of all courses in system
     */
    @GET("courses/")
    suspend fun getCourses(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Course>>

    /**
     * Get Course by Code
     * GET /api/courses/{course_code}/
     *
     * REQUIRES: Authentication
     *
     * NOTE: course_code is the primary key (e.g., "CS101")
     */
    @GET("courses/{id}/")
    suspend fun getCourseByCode(
        @Path("id") courseId: Int
    ): Response<Course>

    // ==================== GROUPS ====================

    /**
     * Get All Groups
     * GET /api/groups/
     *
     * REQUIRES: Authentication
     * PAGINATION: Yes
     */
    @GET("groups/")
    suspend fun getGroups(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Group>>

    /**
     * Get Group by ID
     * GET /api/groups/{id}/
     *
     * REQUIRES: Authentication
     */
    @GET("groups/{id}/")
    suspend fun getGroupById(
        @Path("id") groupId: Int
    ): Response<Group>

    // ==================== ROOMS ====================

    /**
     * Get All Rooms
     * GET /api/rooms/
     *
     * REQUIRES: Authentication
     * PAGINATION: Yes
     */
    @GET("rooms/")
    suspend fun getRooms(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<PaginatedResponse<Room>>

    /**
     * Get Room by ID
     * GET /api/rooms/{id}/
     *
     * REQUIRES: Authentication
     */
    @GET("rooms/{id}/")
    suspend fun getRoomById(
        @Path("id") roomId: Int
    ): Response<Room>

    // ==================== LESSONS (Core Feature) ====================

    /**
     * Get All Lessons
     * GET /api/lessons/
     *
     * REQUIRES: Authentication
     * PAGINATION: Yes
     *
     * FILTERING (Query Parameters):
     * - date: Filter by specific date (YYYY-MM-DD)
     * - course: Filter by course ID
     * - lecturer: Filter by lecturer ID
     * - groups: Filter by group ID
     * - date__gte: Lessons on or after date
     * - date__lte: Lessons on or before date
     *
     * EXAMPLE:
     * /api/lessons/?date=2026-02-13
     * /api/lessons/?lecturer=L001
     * /api/lessons/?date__gte=2026-02-13&date__lte=2026-02-20
     */
    @GET("lessons/")
    suspend fun getLessons(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("date") date: String? = null,
        @Query("course") courseId: Int? = null,
        @Query("lecturer") lecturerId: String? = null,
        @Query("groups") groupId: Int? = null,
        @Query("date__gte") dateFrom: String? = null,
        @Query("date__lte") dateTo: String? = null
    ): Response<PaginatedResponse<Lesson>>

    /**
     * Get Lesson by ID
     * GET /api/lessons/{id}/
     *
     * REQUIRES: Authentication
     */
    @GET("lessons/{id}/")
    suspend fun getLessonById(
        @Path("id") lessonId: Int
    ): Response<Lesson>

    /**
     * Create Lesson
     * POST /api/lessons/
     *
     * REQUIRES: Authentication (lecturer only)
     *
     * VALIDATION:
     * - Date must be in future
     * - Start time before end time
     * - Room must exist and be available
     * - Groups must exist
     * - Lecturer must exist
     */
    @POST("lessons/")
    suspend fun createLesson(
        @Body request: LessonCreateRequest
    ): Response<Lesson>

    /**
     * Update Lesson
     * PUT /api/lessons/{id}/
     *
     * REQUIRES: Authentication (lecturer who created it)
     *
     * Full update - all fields required
     * Triggers notification to students
     */
    @PUT("lessons/{id}/")
    suspend fun updateLesson(
        @Path("id") lessonId: Int,
        @Body request: LessonUpdateRequest
    ): Response<Lesson>

    /**
     * Partial Update Lesson
     * PATCH /api/lessons/{id}/
     *
     * REQUIRES: Authentication (lecturer who created it)
     *
     * Partial update - only include fields to change
     */
    @PATCH("lessons/{id}/")
    suspend fun patchLesson(
        @Path("id") lessonId: Int,
        @Body request: Map<String, Any>  // Flexible partial update
    ): Response<Lesson>

    /**
     * Delete Lesson
     * DELETE /api/lessons/{id}/
     *
     * REQUIRES: Authentication (lecturer who created it)
     *
     * Triggers cancellation notification to students
     * Lesson is soft-deleted (marked as deleted, not removed)
     */
    @DELETE("lessons/{id}/")
    suspend fun deleteLesson(
        @Path("id") lessonId: Int
    ): Response<Unit>

    // ==================== NOTIFICATIONS ====================

    /**
     * Get All Notifications
     * GET /api/notifications/
     *
     * REQUIRES: Authentication
     * PAGINATION: Yes
     *
     * Students see notifications for their group's lessons
     * Lecturers see notifications they created
     *
     * ORDERING: Most recent first
     */
    @GET("notifications/")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("is_sent") isSent: Boolean? = null,
        @Query("message_type") messageType: String? = null
    ): Response<PaginatedResponse<Notification>>

    /**
     * Get Notification by ID
     * GET /api/notifications/{id}/
     *
     * REQUIRES: Authentication
     */
    @GET("notifications/{id}/")
    suspend fun getNotificationById(
        @Path("id") notificationId: Int
    ): Response<Notification>
}

/**
 * API USAGE PATTERNS:
 *
 * // Login
 * val response = apiService.login(LoginRequest("S001", "password"))
 * if (response.isSuccessful) {
 *     val tokens = response.body()
 *     // Store tokens
 * }
 *
 * // Get today's lessons
 * val today = LocalDate.now().toString()
 * val response = apiService.getLessons(date = today)
 *
 * // Get student's lessons (by group)
 * val profile = apiService.getMyProfile().body()
 * val student = apiService.getStudentById(profile.userId).body()
 * val lessons = apiService.getLessons(groupId = student.groupId)
 *
 * // Create lesson
 * val lesson = LessonCreateRequest(
 *     courseId = 1,
 *     lecturerId = "L001",
 *     groupIds = listOf(1, 2),
 *     roomId = 5,
 *     lessonType = "Lecture",
 *     date = "2026-02-14",
 *     startTime = "09:00:00",
 *     endTime = "11:00:00"
 * )
 * apiService.createLesson(lesson)
 */
