package com.example.collis.data.remote.interceptor


import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.data.remote.dto.LoginResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * JWT Authentication Interceptor
 *
 * RESPONSIBILITIES:
 * 1. Add Authorization header to all requests
 * 2. Handle 401 Unauthorized responses
 * 3. Clear session if unauthorized
 *
 * TOKEN FORMAT:
 * Your PythonAnywhere backend uses "Token <token>" format (DRF Token Authentication).
 */
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        /**
         * Skip authentication for public endpoints
         */
        if (shouldSkipAuth(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        /**
         * Get access token from DataStore
         */
        val accessToken = runBlocking {
            preferencesManager.getAccessToken()
        }

        /**
         * Add Authorization header
         *
         * Django Rest Framework Token Authentication uses "Token <key>"
         */
        val authenticatedRequest = originalRequest.newBuilder()
            .apply {
                if (!accessToken.isNullOrBlank()) {
                    addHeader("Authorization", "Token $accessToken")
                }
            }
            .build()

        /**
         * Execute request
         */
        val response = chain.proceed(authenticatedRequest)

        /**
         * Handle 401 Unauthorized
         * If the server returns 401, it means the token is invalid or expired.
         * For TokenAuthentication, we just clear the session.
         */
        if (response.code == 401 && !shouldSkipAuth(originalRequest)) {
            runBlocking {
                preferencesManager.clearSession()
            }
        }

        return response
    }

    /**
     * Check if request should skip authentication
     *
     * Public endpoints that don't need auth:
     * - Login: /api/token/
     */
    private fun shouldSkipAuth(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/token/")
    }
}
