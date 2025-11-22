package com.johnmaronga.bookflow.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    /**
     * Schedule periodic book sync
     * Runs every 24 hours when device has network connection
     */
    fun scheduleSyncWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncBooksWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(SyncBooksWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncBooksWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    /**
     * Schedule daily reading reminder notifications
     * Runs every day at a specific time
     */
    fun scheduleReadingReminders(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(NotificationWorker.TAG)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWorkRequest
        )
    }

    /**
     * Cancel all scheduled work
     */
    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    /**
     * Cancel sync work
     */
    fun cancelSyncWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SyncBooksWorker.WORK_NAME)
    }

    /**
     * Cancel reading reminders
     */
    fun cancelReadingReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationWorker.WORK_NAME)
    }

    /**
     * Calculate initial delay to schedule notification at 8 PM
     * For now, returns 0 to start immediately for testing
     */
    private fun calculateInitialDelay(): Long {
        // TODO: Calculate delay to next 8 PM
        // For now, return 0 for immediate execution (testing)
        return 0L
    }
}
