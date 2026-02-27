package com.example.collis.core.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.core.receiver.TaskReminderReceiver
import com.example.collis.domain.repository.TaskRepository
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.core.net.toUri

/**
 * Utility for scheduling task reminder alarms via AlarmManager
 * 
 * PERSISTENCE:
 * Alarms set with AlarmManager persist even if the app is killed.
 * They are only cleared if the user "Force Stops" the app or on reboot.
 * BootReceiver handles rescheduling after reboot.
 */
object AlarmScheduler {

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleTaskReminder(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskDescription: String?,
        reminderTime: LocalDateTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_REMINDER
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskReminderReceiver.EXTRA_TASK_DESCRIPTION, taskDescription)
            // Ensure the intent is unique for this taskId
            data = "task://$taskId".toUri()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMillis = reminderTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        /**
         * Use setExactAndAllowWhileIdle to ensure the alarm fires even in Doze mode.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_REMINDER
            data = "task://$taskId".toUri()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun snoozeReminder(
        context: Context,
        taskId: Long,
        taskTitle: String,
        taskDescription: String?,
        snoozeMinutes: Int = 10
    ) {
        val snoozeTime = LocalDateTime.now().plusMinutes(snoozeMinutes.toLong())
        scheduleTaskReminder(context, taskId, taskTitle, taskDescription, snoozeTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun rescheduleAllReminders(context: Context, taskRepository: TaskRepository) {
        val tasks = taskRepository.getTasksWithReminders()
        val now = LocalDateTime.now()

        tasks.forEach { task ->
            val reminderTime = task.reminderTime
            if (reminderTime != null && reminderTime.isAfter(now) && !task.isCompleted) {
                scheduleTaskReminder(
                    context = context,
                    taskId = task.id,
                    taskTitle = task.title,
                    taskDescription = task.description,
                    reminderTime = reminderTime
                )
            }
        }
    }
}
