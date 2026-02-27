package com.example.collis.presentation.ui.onboarding


import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Onboarding Page Data Model
 *
 * CONTENT STRATEGY:
 * - Page 1: Welcome & value proposition
 * - Page 2: Key features
 * - Page 3: Call to action
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    // @DrawableRes val image: Int // For custom illustrations
)

/**
 * Onboarding Content
 *
 * BEST PRACTICES:
 * - 3 pages maximum (users get impatient)
 * - Clear, concise messaging
 * - Visual icons/illustrations
 * - Focus on benefits, not features
 */
val onboardingPages = listOf(
    OnboardingPage(
        title = "Welcome to Collis",
        description = "Your all-in-one platform for managing college schedules, tasks, and announcements. Stay organized and never miss a class!",
        icon = Icons.Default.School
    ),
    OnboardingPage(
        title = "Live Schedule Tracking",
        description = "View your daily schedule, get real-time updates on class changes, and see which lessons are happening right now.",
        icon = Icons.Default.Schedule
    ),
    OnboardingPage(
        title = "Stay on Top of Tasks",
        description = "Create tasks with reminders, track your progress, and manage your academic workload efficiently. Let's get started!",
        icon = Icons.Default.TaskAlt
    )
)

/**
 * CUSTOMIZATION IDEAS:
 * - "Never miss a class"
 * - "Track your assignments"
 * - "Get instant notifications"
 */