package com.example.collis.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.collis.core.util.NotificationHelper
import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.repository.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Background worker that polls the server for new announcements
 * and shows system notifications for each new one.
 */
@HiltWorker
class NotificationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationRepository: NotificationRepository,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("NotificationSyncWorker", "Starting background sync...")
        return try {
            doSyncWork()
        } catch (e: Exception) {
            Log.e("NotificationSyncWorker", "Sync failed: ${e.message}")
            Result.success()
        }
    }

    private suspend fun doSyncWork(): Result {
        val notificationsEnabled = preferencesManager.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()

        val token = preferencesManager.getAccessToken()
        if (token.isNullOrBlank()) return Result.success()

        // Fetch notifications (Repository sorts descending by ID, newest first)
        val result = notificationRepository.getNotifications()
            .first { it !is NetworkResult.Loading }

        if (result is NetworkResult.Success) {
            val notifications = result.data ?: emptyList()
            val lastId = preferencesManager.getLastAlertedNotificationId()
            
            Log.d("NotificationSyncWorker", "Fetched ${notifications.size} items. lastId=$lastId")

            // Show system notifications for each new announcement (ID > lastId)
            val newNotifications = notifications
                .filter { it.id > lastId }
                .sortedBy { it.id } // Process in chronological order

            if (newNotifications.isNotEmpty()) {
                Log.d("NotificationSyncWorker", "Found ${newNotifications.size} new notifications!")
                
                // If it's the first sync (lastId=0), only show the latest 3 to avoid spam
                val toShow = if (lastId == 0) newNotifications.takeLast(3) else newNotifications

                toShow.forEach { announcement ->
                    NotificationHelper.showAnnouncementNotification(
                        context = applicationContext,
                        notificationId = announcement.id,
                        title = announcement.messageTypeDisplay.ifBlank { "New Announcement" },
                        message = "${announcement.courseCode}: ${announcement.messageText}"
                    )
                }

                // Update last alerted ID to the absolute latest one from the server
                preferencesManager.saveLastAlertedNotificationId(notifications.maxOf { it.id })
            }

            return Result.success()
        }

        return if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        private const val WORK_NAME = "notification_sync_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<NotificationSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
