package com.example.collis.presentation.ui.student.profile

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val username: String = "",
    val groupName: String = "",
    val userType: String = "",
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = true
)
