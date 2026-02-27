package com.example.collis.data.repository


import com.example.collis.data.network.NetworkResult
import com.example.collis.data.remote.api.CollisApiService
import com.example.collis.data.remote.dto.Notification
import com.example.collis.data.remote.dto.toLocalDateTime
import com.example.collis.domain.model.AnnouncementModel
import com.example.collis.domain.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification/Announcement Repository
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: CollisApiService
) : NotificationRepository {

    override fun getNotifications(): Flow<NetworkResult<List<AnnouncementModel>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getNotifications(pageSize = 100)

            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                val notifications = results.map { it.toDomainModel() }
                val sortedNotifications = notifications.sortedByDescending { it.id }
                emit(NetworkResult.Success(sortedNotifications))
            } else {
                emit(NetworkResult.Error("Failed to load notifications: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error occurred"))
        }
    }

    override suspend fun getUnreadCount(): NetworkResult<Int> {
        return try {
            val response = apiService.getNotifications(pageSize = 1)
            if (response.isSuccessful) {
                val count = response.body()?.count ?: 0
                NetworkResult.Success(count)
            } else {
                NetworkResult.Error("Failed to get count")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getNotificationById(notificationId: Int): NetworkResult<AnnouncementModel> {
        return try {
            val response = apiService.getNotificationById(notificationId)
            if (response.isSuccessful) {
                val notification = response.body()?.toDomainModel()
                    ?: return NetworkResult.Error("Notification not found")
                NetworkResult.Success(notification)
            } else {
                NetworkResult.Error("Failed to load notification")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override fun getNotificationsByType(
        messageType: String
    ): Flow<NetworkResult<List<AnnouncementModel>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apiService.getNotifications(
                messageType = messageType,
                pageSize = 100
            )
            if (response.isSuccessful) {
                val notifications = response.body()?.results?.map { it.toDomainModel() }
                    ?: emptyList()
                emit(NetworkResult.Success(notifications.sortedByDescending { it.id }))
            } else {
                emit(NetworkResult.Error("Failed to load notifications"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(NetworkResult.Error(e.message ?: "Network error"))
        }
    }

    override suspend fun refreshNotifications(): NetworkResult<Unit> {
        return try {
            val response = apiService.getNotifications(pageSize = 1)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error("Server unreachable")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}

/**
 * DTO to Domain Model Mapper
 */
private fun Notification.toDomainModel(): AnnouncementModel {
    return AnnouncementModel(
        id = this.id,
        courseCode = this.courseCode,
        courseTitle = this.courseTitle,
        lessonDate = this.lessonDate,
        lessonTime = this.lessonTime,
        groupNames = this.groupNames.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        messageType = this.messageType,
        messageTypeDisplay = this.messageTypeDisplay,
        messageText = this.messageText,
        createdAt = this.createdAt.toLocalDateTime() ?: java.time.LocalDateTime.now()
    )
}
