package com.example.collis.presentation.ui.auth

import com.example.collis.domain.model.UserProfile

/**
 * Login Screen UI State
 *
 * SEALED CLASS PATTERN:
 * Represents all possible states of login screen
 * Compiler ensures we handle all cases
 */
sealed class LoginUiState {
    /**
     * Initial state - user hasn't attempted login yet
     */
    data object Idle : LoginUiState()

    /**
     * Login in progress
     * Show loading indicator, disable inputs
     */
    data object Loading : LoginUiState()

    /**
     * Login successful
     * Navigate to appropriate home screen
     *
     * @param userProfile User's profile data
     */
    data class Success(val userProfile: UserProfile) : LoginUiState()

    /**
     * Login failed
     * Show error message, enable retry
     *
     * @param message Error message to display
     */
    data class Error(val message: String) : LoginUiState()
}

