package com.johnmaronga.bookflow.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.johnmaronga.bookflow.data.local.AppDatabase
import com.johnmaronga.bookflow.data.remote.RetrofitClient
import com.johnmaronga.bookflow.data.repository.BookRepositoryImpl

class SyncBooksWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SyncBooksWorker"
        const val WORK_NAME = "sync_books_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting book sync...")

        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = BookRepositoryImpl(
                bookDao = database.bookDao(),
                readingProgressDao = database.readingProgressDao(),
                reviewDao = database.reviewDao(),
                apiService = RetrofitClient.bookApiService
            )

            // Sync books from API
            repository.syncBooks().fold(
                onSuccess = {
                    Log.d(TAG, "Book sync completed successfully")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Book sync failed: ${error.message}", error)
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sync: ${e.message}", e)
            Result.failure()
        }
    }
}
