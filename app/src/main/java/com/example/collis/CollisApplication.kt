package com.example.collis

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.collis.core.receiver.NotificationPollReceiver
import com.example.collis.core.util.NotificationHelper
import com.example.collis.core.worker.NotificationSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CollisApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Create notification channels early
        NotificationHelper.createNotificationChannels(this)

        // Ensure WorkManager is initialized with the custom configuration
        // This is safe even with on-demand initialization enabled.
        try {
            WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: Exception) {
            // Already initialized, ignore
        }

        // Schedule background tasks
        NotificationSyncWorker.schedule(this)
        NotificationPollReceiver.schedule(this)
    }
}
