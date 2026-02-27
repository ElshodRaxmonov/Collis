package com.example.collis.presentation.ui.auth

/**
 * Login Screen UI Events
 *
 * USER ACTIONS:
 * All possible user interactions on login screen
 */
sealed class LoginUiEvent {
    /**
     * User clicked login button
     *
     * @param username Entered username
     * @param password Entered password
     */
    data class Login(
        val username: String,
        val password: String
    ) : LoginUiEvent()

    /**
     * User changed username field
     *
     * Used to clear error state when user starts typing
     */
    data class UsernameChanged(val username: String) : LoginUiEvent()

    /**
     * User changed password field
     */
    data class PasswordChanged(val password: String) : LoginUiEvent()

    /**
     * User toggled password visibility
     */
    data object TogglePasswordVisibility : LoginUiEvent()

    /**
     * User dismissed error message
     */
    data object DismissError : LoginUiEvent()

    /**
     * Navigate to forgot password
     * (Future feature)
     */
    data object NavigateToForgotPassword : LoginUiEvent()
}

/**
 * One-time events emitted by LoginViewModel.
 * Consumed once by UI (navigation, snackbar), don't persist in state.
 */
sealed class LoginOneTimeEvent {
    /** Navigate to student home after successful login */
    data object NavigateToStudentHome : LoginOneTimeEvent()

    /** Show a transient message via snackbar */
    data class ShowSnackbar(val message: String) : LoginOneTimeEvent()
}