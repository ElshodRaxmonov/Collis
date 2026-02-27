package com.example.collis.core.navigation

/**
 * Navigation Routes - Centralized navigation paths
 */
sealed class Screen(val route: String) {

    // ==================== ONBOARDING & AUTH ====================

    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")

    // ==================== STUDENT SCREENS ====================

    data object StudentHome : Screen("student/home")
    data object StudentSubjects : Screen("student/subjects")
    data object StudentTasks : Screen("student/tasks")
    data object StudentProfile : Screen("student/profile")
    data object StudentNotifications : Screen("student/notifications")

    data object TaskDetail : Screen("student/tasks/{taskId}") {
        fun createRoute(taskId: Long) = "student/tasks/$taskId"
        const val ARG_TASK_ID = "taskId"
    }

    /**
     * Add/Edit Task Screen
     * 
     * ARGUMENTS:
     * - taskId: (Optional) ID of task to edit
     * - subjectCode: (Optional) Pre-selected subject when creating from subject detail
     */
    data object AddEditTask : Screen("student/tasks/add?taskId={taskId}&subjectCode={subjectCode}") {
        fun createRoute(taskId: Long? = null, subjectCode: String? = null): String {
            val base = "student/tasks/add"
            val params = mutableListOf<String>()
            taskId?.let { params.add("taskId=$it") }
            subjectCode?.let { params.add("subjectCode=$it") }
            
            return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
        }

        const val ARG_TASK_ID = "taskId"
        const val ARG_SUBJECT_CODE = "subjectCode"
    }

    data object SubjectDetail : Screen("student/subjects/{subjectId}") {
        fun createRoute(subjectId: String) = "student/subjects/$subjectId"
        const val ARG_SUBJECT_ID = "subjectId"
    }

    data object AnnouncementDetail : Screen("announcements/{announcementId}") {
        fun createRoute(announcementId: String) = "announcements/$announcementId"
        const val ARG_ANNOUNCEMENT_ID = "announcementId"
    }

    // ==================== SHARED SCREENS ====================

    data object Settings : Screen("settings")
    data object ChangePassword : Screen("change-password")
    data object About : Screen("about")
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String,
    val badgeCount: Int? = null
)

object NavigationGraphs {
    const val AUTH_GRAPH = "auth_graph"
    const val STUDENT_GRAPH = "student_graph"
}
