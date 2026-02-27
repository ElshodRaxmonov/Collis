package com.example.collis.domain.usecase.auth

import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.UserProfile
import com.example.collis.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Login Use Case
 *
 * WHY USE CASES?
 * ✅ Encapsulates single business operation
 * ✅ Reusable across multiple ViewModels
 * ✅ Easy to test in isolation
 * ✅ Single Responsibility Principle
 * ✅ Business logic stays out of ViewModel
 *
 * USE CASE PATTERN:
 * - One use case = One business operation
 * - operator fun invoke() makes it callable like function
 * - Validation logic lives here
 * - Coordinates multiple repositories if needed
 *
 * LEARNING: Use Cases are optional but recommended
 * Simple CRUD operations might not need them
 * Complex operations with validation definitely benefit
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute login operation
     *
     * BUSINESS RULES:
     * 1. Username must not be empty
     * 2. Username format validation (optional)
     * 3. Password must not be empty
     * 4. Password minimum length
     *
     * @param username User's login ID
     * @param password User's password
     * @return Result with UserProfile or error message
     */
    suspend operator fun invoke(
        username: String,
        password: String
    ): NetworkResult<UserProfile> {
        /**
         * Input Validation
         * Validate BEFORE making API call
         * Saves network request if input is invalid
         */

        // Trim whitespace
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        // Username validation
        if (trimmedUsername.isBlank()) {
            return NetworkResult.Error("Username cannot be empty")
        }

        // Username format is not validated here — the backend accepts
        // student_id or plain Django username.
        // Let the server decide if the credentials are valid.

        // Password validation
        if (trimmedPassword.isBlank()) {
            return NetworkResult.Error("Password cannot be empty")
        }

        if (trimmedPassword.length < 4) {
            return NetworkResult.Error("Password must be at least 4 characters")
        }

        /**
         * All validations passed
         * Call repository to perform login
         */
        return authRepository.login(trimmedUsername, trimmedPassword)
    }

}

/**
 * USAGE IN VIEWMODEL:
 *
 * class LoginViewModel @Inject constructor(
 *     private val loginUseCase: LoginUseCase
 * ) : ViewModel() {
 *
 *     fun login(username: String, password: String) {
 *         viewModelScope.launch {
 *             _uiState.value = LoginUiState.Loading
 *
 *             val result = loginUseCase(username, password)
 *
 *             _uiState.value = when (result) {
 *                 is NetworkResult.Success -> LoginUiState.Success(result.data)
 *                 is NetworkResult.Error -> LoginUiState.Error(result.message)
 *                 is NetworkResult.Loading -> LoginUiState.Loading
 *             }
 *         }
 *     }
 * }
 */