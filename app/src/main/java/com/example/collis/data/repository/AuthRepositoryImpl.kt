package com.example.collis.data.repository



import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.data.network.NetworkResult
import com.example.collis.data.remote.api.CollisApiService
import com.example.collis.data.remote.dto.ChangePasswordRequest
import com.example.collis.data.remote.dto.LoginRequest
import com.example.collis.domain.model.UserProfile
import com.example.collis.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository Implementation
 *
 * RESPONSIBILITIES:
 * 1. Handle login flow
 * 2. Manage user session
 * 3. Handle password changes
 * 4. Provide user info to UI
 *
 * CLEAN ARCHITECTURE:
 * - Implements interface from domain layer
 * - Depends on data sources (API, DataStore)
 * - Maps DTOs to domain models
 * - Handles errors gracefully
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: CollisApiService,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    /**
     * Login Flow
     *
     * STEPS:
     * 1. Call login API with credentials
     * 2. Receive JWT tokens
     * 3. Call profile API to get user info
     * 4. Save tokens + user info to DataStore
     * 5. Return success
     *
     * ERROR HANDLING:
     * - Network errors
     * - Invalid credentials (401)
     * - Server errors (500)
     * - Parsing errors
     */
    override suspend fun login(
        username: String,
        password: String
    ): NetworkResult<UserProfile> {
        return try {
            /**
             * Step 1: Login and get tokens
             */
            val loginResponse = apiService.login(
                LoginRequest(username, password)
            )

            if (!loginResponse.isSuccessful) {
                val errorBody = try {
                    loginResponse.errorBody()?.string()
                } catch (_: Exception) { null }

                val errorMessage = when (loginResponse.code()) {
                    400 -> {
                        // Backend returns {"error": "Invalid credentials"} on 400
                        if (errorBody?.contains("Invalid credentials") == true) {
                            "Invalid username or password"
                        } else {
                            "Invalid request. Please check your input."
                        }
                    }
                    401 -> "Invalid username or password"
                    500 -> "Server error. Please try again later"
                    else -> "Login failed (code ${loginResponse.code()}). Please try again."
                }
                return NetworkResult.Error(errorMessage)
            }

            val tokens = loginResponse.body()
                ?: return NetworkResult.Error("Failed to receive tokens")

            /**
             * Step 2: Save auth token temporarily
             * Needed for next API call (profile)
             */
            preferencesManager.saveAuthTokens(
                accessToken = tokens.token
            )

            /**
             * Step 3: Get user profile
             * Fetches the user's profile info from the server
             */
            val profileResponse = apiService.getMyProfile()

            if (!profileResponse.isSuccessful) {
                // Login succeeded but profile fetch failed
                // Clear tokens and return error
                preferencesManager.clearSession()
                return NetworkResult.Error("Failed to fetch user profile")
            }

            val profile = profileResponse.body()
                ?: return NetworkResult.Error("Invalid profile data")

            /**
             * Step 4: Save complete session
             */
            val effectiveUserId = profile.userId ?: profile.username  // Fallback for ADMIN users
            preferencesManager.saveUserSession(
                accessToken = tokens.token,
                userId = effectiveUserId,
                userType = profile.userType,
                username = profile.username,
                fullName = profile.fullName,
                email = profile.email,
                groupName = profile.groupName
            )

            /**
             * Step 5: Map to domain model and return
             */
            val userProfile = UserProfile(
                id = effectiveUserId,
                username = profile.username,
                fullName = profile.fullName,
                email = profile.email,
                userType = profile.userType,
                userTypeDisplay = profile.userTypeDisplay,
                groupName = profile.groupName
            )

            NetworkResult.Success(userProfile)

        } catch (e: Exception) {
            /**
             * Network or unexpected errors
             * Clear any partially saved token
             */
            try { preferencesManager.clearSession() } catch (_: Exception) {}
            NetworkResult.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    /**
     * Logout Flow
     *
     * STEPS:
     * 1. Clear local session (tokens + user info)
     * 2. Optionally call server logout endpoint
     *
     * NOTE: Even if server call fails, we clear local session
     * This ensures user can always logout locally
     */
    override suspend fun logout(): NetworkResult<Unit> {
        return try {
            /**
             * Clear local session first
             * This ensures user is logged out even if network fails
             */
            preferencesManager.clearSession()

            /**
             * Try to notify server (optional)
             * Server can invalidate token on their side
             *
             * If this fails, we don't care - user is already logged out locally
             */
            try {
                apiService.logout()
            } catch (e: Exception) {
                // Ignore server errors on logout
            }

            NetworkResult.Success(Unit)

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Logout failed")
        }
    }

    /**
     * Change Password Flow
     *
     * STEPS:
     * 1. Call change password API
     * 2. Return success/error
     *
     * NOTE: User stays logged in, tokens don't change
     */
    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): NetworkResult<String> {
        return try {
            val response = apiService.changePassword(
                ChangePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )
            )

            if (response.isSuccessful) {
                val message = response.body()?.message
                    ?: "Password changed successfully"
                NetworkResult.Success(message)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid password format"
                    401 -> "Current password is incorrect"
                    else -> "Failed to change password"
                }
                NetworkResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An error occurred")
        }
    }

    /**
     * Get current user profile (reactive)
     *
     * REACTIVE PATTERN:
     * Returns Flow that emits whenever user data changes
     * UI automatically updates
     */
    override fun getCurrentUser(): Flow<UserProfile?> {
        return preferencesManager.isLoggedIn.map { isLoggedIn ->
            if (isLoggedIn) {
                // User is logged in, get their info
                val userId = preferencesManager.getUserId()
                val username = preferencesManager.username.map { it }.first()
                val fullName = preferencesManager.fullName.map { it }.first()
                val email = preferencesManager.email.map { it }.first()
                val userType = preferencesManager.getUserType()
                val groupName = preferencesManager.groupName.map { it }.first()

                if (userId != null && userType != null) {
                    UserProfile(
                        id = userId,
                        username = username ?: "",
                        fullName = fullName ?: "",
                        email = email ?: "",
                        userType = userType,
                        userTypeDisplay = "Student",
                        groupName = groupName
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Check if user is logged in (reactive)
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return preferencesManager.isLoggedIn
    }

    /**
     * Check if user is student (reactive)
     */
    override fun isStudent(): Flow<Boolean> {
        return preferencesManager.isStudent
    }

    /**
     * Refresh user profile from server
     *
     * WHEN TO USE:
     * - After profile update
     * - Pull to refresh
     * - App foreground
     */
    override suspend fun refreshProfile(): NetworkResult<UserProfile> {
        return try {
            val response = apiService.getMyProfile()

            if (response.isSuccessful) {
                val profile = response.body()
                    ?: return NetworkResult.Error("Invalid profile data")

                // Update stored profile
                preferencesManager.updateProfile(
                    fullName = profile.fullName,
                    email = profile.email
                )

                val userProfile = UserProfile(
                    id = profile.userId ?: profile.username,
                    username = profile.username,
                    fullName = profile.fullName,
                    email = profile.email,
                    userType = profile.userType,
                    userTypeDisplay = profile.userTypeDisplay,
                    groupName = profile.groupName
                )

                NetworkResult.Success(userProfile)
            } else {
                NetworkResult.Error("Failed to refresh profile")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An error occurred")
        }
    }
}