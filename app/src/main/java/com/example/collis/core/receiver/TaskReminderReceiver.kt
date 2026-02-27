package com.example.collis.core.receiver

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.domain.model.RepeatType
import com.example.collis.domain.repository.TaskRepository
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.collis.core.util.NotificationHelper
import com.example.collis.core.util.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for task reminders
 */
@AndroidEntryPoint
class TaskReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TASK_REMINDER) return

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION)

        if (taskId == -1L) return

        val pendingResult = goAsync()

        /**
         * Use GlobalScope or a dedicated scope for long-running operations in Receiver
         * Here we use CoroutineScope(Dispatchers.IO)
         */
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskRepository.getTaskById(taskId)

                if (task != null && !task.isCompleted) {
                    NotificationHelper.showTaskReminderNotification(
                        context = context,
                        taskId = taskId,
                        taskTitle = taskTitle,
                        taskDescription = taskDescription
                    )

                    if (task.repeatEnabled && task.repeatType != RepeatType.NONE) {
                        val nextReminderTime = task.repeatType.getNextOccurrence(
                            task.reminderTime ?: return@launch
                        )

                        if (nextReminderTime != null) {
                            val updatedTask = task.copy(reminderTime = nextReminderTime)
                            taskRepository.updateTask(updatedTask)

                            AlarmScheduler.scheduleTaskReminder(
                                context = context,
                                taskId = taskId,
                                taskTitle = taskTitle,
                                taskDescription = taskDescription,
                                reminderTime = nextReminderTime
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TASK_REMINDER = "com.collis.ACTION_TASK_REMINDER"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_TASK_DESCRIPTION = "EXTRA_TASK_DESCRIPTION"
    }
}
