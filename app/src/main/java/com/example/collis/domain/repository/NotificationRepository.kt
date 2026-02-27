package com.example.collis.domain.repository


import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.AnnouncementModel
import kotlinx.coroutines.flow.Flow

/**
 * Notification Repository Interface
 */
interface NotificationRepository {

    fun getNotifications(): Flow<NetworkResult<List<AnnouncementModel>>>

    suspend fun getUnreadCount(): NetworkResult<Int>

    suspend fun getNotificationById(notificationId: Int): NetworkResult<AnnouncementModel>

    fun getNotificationsByType(messageType: String): Flow<NetworkResult<List<AnnouncementModel>>>

    suspend fun refreshNotifications(): NetworkResult<Unit>
}

