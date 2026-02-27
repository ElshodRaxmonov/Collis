package com.example.collis.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.usecase.auth.LoginUseCase
import com.example.collis.presentation.ui.auth.LoginOneTimeEvent
import com.example.collis.presentation.ui.auth.LoginUiEvent
import com.example.collis.presentation.ui.auth.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login ViewModel
 *
 * RESPONSIBILITIES:
 * - Hold login screen state
 * - Handle user input events
 * - Execute login use case
 * - Navigate on success
 *
 * STATE MANAGEMENT:
 * - StateFlow for persistent state (UI renders from this)
 * - Channel for one-time events (navigation, snackbar)
 *
 * LEARNING: Channel vs SharedFlow for events
 * - Channel: Suspends if no collectors (buffer = 0)
 * - SharedFlow: Doesn't suspend, can replay events
 * - Channel is better for one-time UI events
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    /**
     * UI State
     * Represents current state of login screen
     */
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Form Fields
     * Separate state for form inputs
     * Allows validation without affecting main state
     */
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    /**
     * One-time events
     *
     * CHANNEL PATTERN:
     * - Events are sent once
     * - Consumed by UI
     * - Don't replay on config change
     *
     * Perfect for navigation, snackbars, dialogs
     */
    private val _oneTimeEvent = Channel<LoginOneTimeEvent>(Channel.BUFFERED)
    val oneTimeEvent = _oneTimeEvent.receiveAsFlow()

    /**
     * Loading state (derived from uiState)
     */
    val isLoading: StateFlow<Boolean> = uiState.map {
        it is LoginUiState.Loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Event Handler
     * Single entry point for all UI events
     */
    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.Login -> login(event.username, event.password)
            is LoginUiEvent.UsernameChanged -> _username.value = event.username
            is LoginUiEvent.PasswordChanged -> _password.value = event.password
            is LoginUiEvent.TogglePasswordVisibility -> togglePasswordVisibility()
            is LoginUiEvent.DismissError -> dismissError()
            is LoginUiEvent.NavigateToForgotPassword -> navigateToForgotPassword()
        }
    }

    /**
     * Performs the login API call and emits navigation or error events.
     */
    private fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = loginUseCase(username, password)) {
                is NetworkResult.Success -> {
                    val userProfile = result.data!!
                    _uiState.value = LoginUiState.Success(userProfile)

                    // All users navigate to student home
                    _oneTimeEvent.send(LoginOneTimeEvent.NavigateToStudentHome)
                }

                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Login failed. Please try again."
                    _uiState.value = LoginUiState.Error(errorMessage)
                    _oneTimeEvent.send(LoginOneTimeEvent.ShowSnackbar(errorMessage))
                }

                is NetworkResult.Loading -> { /* Already in loading state */ }
            }
        }
    }

    /**
     * Toggle password visibility
     */
    private fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    /**
     * Dismiss error
     * Returns to idle state
     */
    private fun dismissError() {
        _uiState.value = LoginUiState.Idle
    }

    /**
     * Navigate to forgot password
     * Future feature
     */
    private fun navigateToForgotPassword() {
        viewModelScope.launch {
            _oneTimeEvent.send(
                LoginOneTimeEvent.ShowSnackbar("Password reset coming soon!")
            )
        }
    }

    /**
     * Real-time validation
     *
     * OPTIONAL FEATURE:
     * Show validation errors as user types
     * Better UX than waiting for submit
     */
    val usernameError: StateFlow<String?> = username.map { username ->
        when {
            username.isBlank() -> null // Don't show error for empty field
            username.length < 2 -> "Username too short"
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val passwordError: StateFlow<String?> = password.map { password ->
        when {
            password.isBlank() -> null
            password.length < 4 -> "Password too short"
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Form validation
     * Enables/disables login button
     */
    val isFormValid: StateFlow<Boolean> = combine(
        username,
        password,
        usernameError,
        passwordError
    ) { username, password, usernameErr, passwordErr ->
        username.isNotBlank() &&
                password.isNotBlank() &&
                usernameErr == null &&
                passwordErr == null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
}

