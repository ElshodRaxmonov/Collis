package com.example.collis.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.collis.core.util.AlarmScheduler
import com.example.collis.core.worker.NotificationSyncWorker
import com.example.collis.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Reschedule alarms and background sync after device reboot
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && 
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Reschedule task reminders
                AlarmScheduler.rescheduleAllReminders(context, taskRepository)
                
                // 2. Ensure notification background sync is active
                NotificationSyncWorker.schedule(context)

                // 3. Restart the AlarmManager backup poll chain
                NotificationPollReceiver.schedule(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
