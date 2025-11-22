package com.johnmaronga.bookflow.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.johnmaronga.bookflow.R
import com.johnmaronga.bookflow.data.local.AppDatabase

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "NotificationWorker"
        const val WORK_NAME = "reading_reminder_work"
        const val CHANNEL_ID = "reading_reminders"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Checking for reading reminders...")

        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val progressDao = database.readingProgressDao()

            // Get currently reading books count
            // Note: We need to collect the flow once
            var currentlyReadingCount = 0
            progressDao.getCurrentlyReading().collect { progressList ->
                currentlyReadingCount = progressList.size
            }

            if (currentlyReadingCount > 0) {
                showReadingReminderNotification(currentlyReadingCount)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking reading reminders: ${e.message}", e)
            Result.failure()
        }
    }

    private fun showReadingReminderNotification(bookCount: Int) {
        createNotificationChannel()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to read!")
            .setContentText("You have $bookCount book${if (bookCount > 1) "s" else ""} in progress. Continue your reading journey!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Reading reminder notification sent")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reading Reminders"
            val descriptionText = "Notifications to remind you to continue reading"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
