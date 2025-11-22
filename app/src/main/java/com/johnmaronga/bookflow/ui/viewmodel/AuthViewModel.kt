package com.johnmaronga.bookflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.johnmaronga.bookflow.data.local.AppDatabase
import com.johnmaronga.bookflow.data.local.SessionManager
import com.johnmaronga.bookflow.data.remote.RetrofitClient
import com.johnmaronga.bookflow.data.repository.AuthRepository
import com.johnmaronga.bookflow.data.repository.BookRepositoryImpl

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val sessionManager by lazy { SessionManager(context) }

    private val bookRepository by lazy {
        BookRepositoryImpl(
            bookDao = database.bookDao(),
            readingProgressDao = database.readingProgressDao(),
            reviewDao = database.reviewDao(),
            apiService = RetrofitClient.bookApiService
        )
    }

    private val authRepository by lazy {
        AuthRepository(
            userDao = database.userDao(),
            sessionManager = sessionManager
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(bookRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
