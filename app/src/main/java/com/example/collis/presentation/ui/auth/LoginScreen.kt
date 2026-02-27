package com.example.collis.presentation.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collis.presentation.components.ButtonVariant
import com.example.collis.presentation.components.CollisButton
import com.example.collis.presentation.components.CollisTextField
import com.example.collis.presentation.viewmodel.LoginViewModel
import mr.dev.collis.R

/**
 * Login Screen
 *
 * FEATURES:
 * - Username & password input
 * - Real-time validation
 * - Loading state
 * - Error handling
 * - Password visibility toggle
 * - Professional design
 *
 * ACCESSIBILITY:
 * - Keyboard navigation
 * - Screen reader support
 * - Proper focus management
 * - Large touch targets
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (userType: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    /**
     * Collect UI State
     *
     * collectAsStateWithLifecycle():
     * - Lifecycle-aware collection
     * - Stops collecting when screen not visible
     * - Saves resources
     */
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val passwordVisible by viewModel.passwordVisible.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()
    val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()

    /**
     * Snackbar Host State
     * For showing error messages
     */
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time navigation and snackbar events from the ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.oneTimeEvent.collect { event ->
            when (event) {
                is LoginOneTimeEvent.NavigateToStudentHome -> {
                    onLoginSuccess("STUDENT")
                }

                is LoginOneTimeEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    /**
     * Main Scaffold
     */
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            /**
             * Login Form Content
             */
            LoginContent(
                username = username,
                password = password,
                passwordVisible = passwordVisible,
                isLoading = isLoading,
                isFormValid = isFormValid,
                usernameError = usernameError,
                passwordError = passwordError,
                uiState = uiState,
                onUsernameChange = {
                    viewModel.onEvent(LoginUiEvent.UsernameChanged(it))
                },
                onPasswordChange = {
                    viewModel.onEvent(LoginUiEvent.PasswordChanged(it))
                },
                onTogglePasswordVisibility = {
                    viewModel.onEvent(LoginUiEvent.TogglePasswordVisibility)
                },
                onLoginClick = {
                    viewModel.onEvent(LoginUiEvent.Login(username, password))
                },
                onForgotPasswordClick = {
                    viewModel.onEvent(LoginUiEvent.NavigateToForgotPassword)
                }
            )
        }
    }
}

/**
 * Login Content
 * Separated for better organization and reusability
 */
@Composable
private fun LoginContent(
    username: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    isFormValid: Boolean,
    usernameError: String?,
    passwordError: String?,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    /**
     * Focus Manager
     * For keyboard navigation (Tab, Next)
     */
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /**
         * Logo & Title Section
         */
        Spacer(modifier = Modifier.height(32.dp))

        /**
         * App Logo
         *
         * TODO: Replace with actual logo
         * For now, using Icon
         */
        Surface(
            modifier = Modifier.size(100.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.collis_logo),
                    contentDescription = "Collis Logo",
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * App Name
         */
        Text(
            text = "Collis",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        /**
         * Tagline
         */
        Text(
            text = "College Live Schedule",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        /**
         * Error Banner
         * Only show if in error state
         */
        if (uiState is LoginUiState.Error) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        /**
         * Username Field
         *
         * FEATURES:
         * - Leading icon
         * - Placeholder text
         * - Real-time validation
         * - Error message
         * - Focus management
         */
        CollisTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Username",
            placeholder = "FIT*****",
            leadingIcon = Icons.Default.Person,
            isError = usernameError != null && username.isNotEmpty(),
            errorMessage = usernameError,
            helperText = "Enter your Student ID (starts with 'FIT')",
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Password Field
         *
         * FEATURES:
         * - Leading lock icon
         * - Trailing visibility toggle
         * - Masked input
         * - Real-time validation
         */
        CollisTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "Enter your password",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (passwordVisible)
                Icons.Default.Visibility
            else
                Icons.Default.VisibilityOff,
            onTrailingIconClick = onTogglePasswordVisibility,
            isError = passwordError != null && password.isNotEmpty(),
            errorMessage = passwordError,
            enabled = !isLoading,
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isFormValid && !isLoading) {
                        onLoginClick()
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        /**
         * Forgot Password Link
         */
        TextButton(
            onClick = onForgotPasswordClick,
            enabled = !isLoading
        ) {
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * Login Button
         *
         * FEATURES:
         * - Loading state (shows spinner)
         * - Disabled when form invalid
         * - Full width
         * - Primary action styling
         */
        CollisButton(
            text = "Login",
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !isLoading,
            loading = isLoading,
            icon = Icons.Default.Login,
            variant = ButtonVariant.Primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        /**
         * Information Text
         */
        Text(
            text = "Don't have an account?\nPlease contact your administrator.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Version Info
         */
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * PREVIEW FUNCTIONS
 * For Android Studio preview
 */
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            username = "FIT2504229",
            password = "password",
            passwordVisible = false,
            isLoading = false,
            isFormValid = true,
            usernameError = null,
            passwordError = null,
            uiState = LoginUiState.Idle,
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    MaterialTheme {
        LoginContent(
            username = "FIT2504229",
            password = "password",
            passwordVisible = false,
            isLoading = true,
            isFormValid = true,
            usernameError = null,
            passwordError = null,
            uiState = LoginUiState.Loading,
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    MaterialTheme {
        LoginContent(
            username = "FIT2504229",
            password = "wrong",
            passwordVisible = false,
            isLoading = false,
            isFormValid = true,
            usernameError = null,
            passwordError = null,
            uiState = LoginUiState.Error("Invalid username or password"),
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {}
        )
    }
}