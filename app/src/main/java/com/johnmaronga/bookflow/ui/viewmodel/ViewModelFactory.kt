package com.johnmaronga.bookflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.johnmaronga.bookflow.data.local.AppDatabase
import com.johnmaronga.bookflow.data.remote.RetrofitClient
import com.johnmaronga.bookflow.data.repository.BookRepositoryImpl

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val database by lazy { AppDatabase.getDatabase(context) }

    private val repository by lazy {
        BookRepositoryImpl(
            bookDao = database.bookDao(),
            readingProgressDao = database.readingProgressDao(),
            reviewDao = database.reviewDao(),
            apiService = RetrofitClient.bookApiService
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
