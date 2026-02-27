package com.example.collis.core.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.example.collis.core.worker.NotificationSyncWorker

/**
 * AlarmManager-based backup that fires every ~15 minutes
 * using [setExactAndAllowWhileIdle] — this bypasses Doze mode
 * and runs even when the phone is locked or the app is killed.
 *
 * Each trigger enqueues a one-time [NotificationSyncWorker] via
 * WorkManager and reschedules itself for the next interval.
 * Duplicate notification delivery is prevented via lastNotificationId.
 */
class NotificationPollReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Trigger an immediate WorkManager sync
        NotificationSyncWorker.runOnce(context)

        // Reschedule for next interval
        scheduleNext(context)
    }

    companion object {
        private const val REQUEST_CODE = 9999
        private const val INTERVAL_MS = 15L * 60 * 1_000 // 15 minutes

        /**
         * Start the repeating alarm chain.
         * Safe to call multiple times — it overwrites the previous alarm.
         */
        fun schedule(context: Context) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = buildPendingIntent(context)
            val triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MS

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        }

        /** Cancel the alarm chain. */
        fun cancel(context: Context) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(buildPendingIntent(context))
        }

        private fun scheduleNext(context: Context) = schedule(context)

        private fun buildPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, NotificationPollReceiver::class.java)
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
