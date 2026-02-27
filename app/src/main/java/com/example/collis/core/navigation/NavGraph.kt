package com.example.collis.core.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.collis.core.worker.NotificationSyncWorker
import com.example.collis.presentation.ui.auth.LoginScreen
import com.example.collis.presentation.ui.onboarding.OnboardingScreen
import com.example.collis.presentation.ui.splash.SplashScreen
import com.example.collis.presentation.ui.student.home.HomeScreen
import com.example.collis.presentation.ui.student.notification.NotificationScreen
import com.example.collis.presentation.ui.student.profile.ProfileScreen
import com.example.collis.presentation.ui.student.subject.SubjectsScreen
import com.example.collis.presentation.ui.student.task.AddEditTaskScreen
import com.example.collis.presentation.ui.student.task.TaskManagerScreen

/**
 * Navigation Graph Setup
 *
 * Defines the complete navigation structure of the student-focused Collis app.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String = NavigationGraphs.AUTH_GRAPH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ==================== AUTHENTICATION FLOW ====================
        navigation(
            startDestination = Screen.Splash.route,
            route = NavigationGraphs.AUTH_GRAPH
        ) {
            composable(route = Screen.Splash.route) {
                val context = LocalContext.current
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToStudentHome = {
                        // User is already logged in — trigger immediate sync
                        NotificationSyncWorker.runOnce(context)
                        navController.navigate(NavigationGraphs.STUDENT_GRAPH) {
                            popUpTo(NavigationGraphs.AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.Login.route) {
                val context = LocalContext.current
                LoginScreen(
                    onLoginSuccess = { _ ->
                        // Trigger immediate notification sync after login
                        NotificationSyncWorker.runOnce(context)
                        navController.navigate(NavigationGraphs.STUDENT_GRAPH) {
                            popUpTo(NavigationGraphs.AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ==================== STUDENT FLOW ====================
        navigation(
            startDestination = Screen.StudentHome.route,
            route = NavigationGraphs.STUDENT_GRAPH
        ) {
            composable(route = Screen.StudentHome.route) {
                StudentHomeWithBottomNav(navController = navController)
            }

            composable(
                route = Screen.AddEditTask.route,
                arguments = listOf(
                    navArgument(Screen.AddEditTask.ARG_TASK_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(Screen.AddEditTask.ARG_SUBJECT_CODE) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString(Screen.AddEditTask.ARG_TASK_ID)?.toLongOrNull()

                AddEditTaskScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() },
                    onTaskSaved = { /* Optional: handle additional refresh if needed */ }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudentHomeWithBottomNav(navController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            StudentBottomNavigation(
                navController = bottomNavController,
                onNavigate = { route ->
                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.StudentHome.route,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(Screen.StudentHome.route) {
                HomeScreen(
                    onNavigateToTaskDetail = { taskId ->
                        navController.navigate(Screen.AddEditTask.createRoute(taskId))
                    },
                    onNavigateToAnnouncement = { _ -> },
                    onNavigateToSubject = { _ ->
                        bottomNavController.navigate(Screen.StudentSubjects.route)
                    },
                    onNavigateToAllTasks = {
                        bottomNavController.navigate(Screen.StudentTasks.route)
                    },
                    onNavigateToAllAnnouncements = {
                        bottomNavController.navigate(Screen.StudentNotifications.route)
                    }
                )
            }

            composable(Screen.StudentSubjects.route) {
                SubjectsScreen(
                    onNavigateToAddTask = { subjectCode ->
                        // Navigate to Add Task with pre-selected subject
                        navController.navigate(Screen.AddEditTask.createRoute(subjectCode = subjectCode))
                    }
                )
            }

            composable(Screen.StudentNotifications.route) {
                NotificationScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }

            composable(Screen.StudentTasks.route) {
                TaskManagerScreen(
                    onNavigateToTaskDetail = { taskId ->
                        navController.navigate(Screen.AddEditTask.createRoute(taskId))
                    },
                    onNavigateToAddTask = {
                        navController.navigate(Screen.AddEditTask.createRoute())
                    }
                )
            }

            composable(Screen.StudentProfile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(NavigationGraphs.AUTH_GRAPH) {
                            popUpTo(NavigationGraphs.STUDENT_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
