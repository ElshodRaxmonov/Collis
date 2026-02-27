package com.example.collis.domain.repository

import com.example.collis.data.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(username: String, password: String): NetworkResult<com.example.collis.domain.model.UserProfile>

    suspend fun logout(): NetworkResult<Unit>

    suspend fun changePassword(oldPassword: String, newPassword: String): NetworkResult<String>

    fun getCurrentUser(): Flow<com.example.collis.domain.model.UserProfile?>

    fun isLoggedIn(): Flow<Boolean>

    fun isStudent(): Flow<Boolean>

    suspend fun refreshProfile(): NetworkResult<com.example.collis.domain.model.UserProfile>
}