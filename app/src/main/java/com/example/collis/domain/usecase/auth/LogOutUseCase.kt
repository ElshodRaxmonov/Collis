package com.example.collis.domain.usecase.auth

import com.example.collis.domain.repository.AuthRepository

import com.example.collis.data.network.NetworkResult
import javax.inject.Inject

/**
 * Logout Use Case
 *
 * SIMPLE USE CASE:
 * No validation needed, just delegates to repository
 * Still useful for consistency and future extensibility
 *
 * FUTURE ENHANCEMENTS:
 * - Clear cached data
 * - Cancel pending operations
 * - Clean up resources
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute logout operation
     *
     * BUSINESS LOGIC:
     * 1. Clear local session
     * 2. Optionally notify server
     * 3. Always succeed locally (even if server call fails)
     *
     * @return Always success (logout never fails locally)
     */
    suspend operator fun invoke(): NetworkResult<Unit> {
        return authRepository.logout()
    }
}