package com.example.collis.core.receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.collis.core.util.AlarmScheduler
import com.example.collis.core.util.NotificationHelper
import com.example.collis.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles notification action buttons
 *
 * ACTIONS:
 * - Mark Complete: Complete task from notification
 * - Snooze: Delay reminder by 10 minutes
 *
 * USER EXPERIENCE:
 * User can interact with notification without opening app
 * Quick actions increase productivity
 */
@AndroidEntryPoint
class TaskActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_COMPLETE -> {
                        // Mark task as completed
                        taskRepository.toggleTaskCompletion(taskId)

                        // Cancel notification
                        NotificationHelper.cancelNotification(context, taskId.toInt())

                        // Cancel any scheduled alarms
                        AlarmScheduler.cancelTaskReminder(context, taskId)
                    }

                    ACTION_SNOOZE -> {
                        // Get task details
                        val task = taskRepository.getTaskById(taskId)

                        if (task != null) {
                            // Snooze for 10 minutes
                            AlarmScheduler.snoozeReminder(
                                context = context,
                                taskId = taskId,
                                taskTitle = task.title,
                                taskDescription = task.description,
                                snoozeMinutes = 10
                            )

                            // Cancel current notification
                            NotificationHelper.cancelNotification(context, taskId.toInt())
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_COMPLETE = "com.collis.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.collis.ACTION_SNOOZE"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    }
}