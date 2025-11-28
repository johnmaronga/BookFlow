package com.johnmaronga.bookflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.johnmaronga.bookflow.data.local.AppDatabase
import com.johnmaronga.bookflow.data.remote.RetrofitClient
import com.johnmaronga.bookflow.data.repository.BookRepositoryImpl
import com.johnmaronga.bookflow.ui.screens.AuthScreen
import com.johnmaronga.bookflow.ui.screens.BookDetailsScreen
import com.johnmaronga.bookflow.ui.screens.DashboardScreen
import com.johnmaronga.bookflow.ui.screens.ProfileScreen
import com.johnmaronga.bookflow.ui.screens.WelcomeScreen
import com.johnmaronga.bookflow.ui.viewmodel.AuthViewModel
import com.johnmaronga.bookflow.ui.viewmodel.DashboardViewModel
import com.johnmaronga.bookflow.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }
    object Profile : Screen("profile")
}

@Composable
fun BookFlowNavigation(
    onWebSearchRequest: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(context)
    )

    // Reset auth state when navigating away from auth screen
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                Screen.Dashboard.route -> {
                    // User successfully navigated away from auth, ensure clean state
                    authViewModel.clearFormErrors()
                }
                Screen.Welcome.route -> {
                    // User went back to welcome, clear auth state
                    authViewModel.resetState()
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        welcomeScreen(navController)
        authScreen(navController, authViewModel)
        dashboardScreen(navController, onWebSearchRequest)
        profileScreen(navController, authViewModel)
        bookDetailsScreen(navController)
    }
}

private fun NavGraphBuilder.welcomeScreen(
    navController: androidx.navigation.NavHostController
) {
    composable(Screen.Welcome.route) {
        WelcomeScreen(
            onGetStartedClick = {
                navController.navigate(Screen.Auth.route)
            }
        )
    }
}

private fun NavGraphBuilder.authScreen(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Screen.Auth.route) {
        AuthScreen(
            onSignInSuccess = {
                // Navigate to dashboard after successful sign in
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            },
            onSignUpSuccess = {
                // Navigate to dashboard after successful sign up
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            },
            onBackClick = {
                // Clear form when going back to welcome
                authViewModel.resetState()
                navController.popBackStack()
            },
            onSkipForNowClick = {
                // Navigate to dashboard as guest
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            },
            viewModel = authViewModel
        )
    }
}

private fun NavGraphBuilder.dashboardScreen(
    navController: androidx.navigation.NavHostController,
    onWebSearchRequest: () -> Unit
) {
    composable(Screen.Dashboard.route) {
        DashboardScreen(
            onSeeAllClick = { section ->
                // TODO: Implement detailed list navigation when ready
                // For now, you can add navigation here for different sections
                when (section) {
                    "search" -> {
                        // Navigate to search screen
                    }
                    "want_to_read" -> {
                        // Navigate to want to read list
                    }
                    "currently_reading" -> {
                        // Navigate to currently reading list
                    }
                    "trending" -> {
                        // Navigate to trending books
                    }
                    "reviews" -> {
                        // Navigate to reviews list
                    }
                }
            },
            onBookClick = { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            },
            onWebSearchClick = onWebSearchRequest,
            onProfileClick = {
                navController.navigate(Screen.Profile.route)
            }
        )
    }
}

private fun NavGraphBuilder.profileScreen(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Screen.Profile.route) {
        ProfileScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onLogout = {
                // Call logout on ViewModel and navigate to welcome
                authViewModel.logout()
                navController.navigate(Screen.Welcome.route) {
                    // Clear entire back stack and start fresh
                    popUpTo(0)
                }
            }
        )
    }
}

private fun NavGraphBuilder.bookDetailsScreen(
    navController: androidx.navigation.NavHostController
) {
    composable(Screen.BookDetails.route) { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val context = LocalContext.current
        val viewModel: DashboardViewModel = viewModel(
            factory = ViewModelFactory(context)
        )

        var book by remember { mutableStateOf<com.johnmaronga.bookflow.data.model.Book?>(null) }
        var progress by remember { mutableStateOf<com.johnmaronga.bookflow.data.model.ReadingProgress?>(null) }
        var review by remember { mutableStateOf<com.johnmaronga.bookflow.data.model.Review?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(bookId) {
            scope.launch {
                val database = AppDatabase.getDatabase(context)
                val repository = BookRepositoryImpl(
                    bookDao = database.bookDao(),
                    readingProgressDao = database.readingProgressDao(),
                    reviewDao = database.reviewDao(),
                    apiService = RetrofitClient.bookApiService
                )
                book = repository.getBookById(bookId)
                progress = repository.getProgressByBookId(bookId)
                review = repository.getReviewByBookId(bookId)
            }
        }

        book?.let { bookData ->
            BookDetailsScreen(
                book = bookData,
                progress = progress,
                review = review,
                onBackClick = { navController.popBackStack() },
                onUpdateProgress = { newProgress ->
                    viewModel.updateReadingProgress(newProgress)
                },
                onSaveReview = { newReview ->
                    viewModel.addOrUpdateReview(newReview)
                }
            )
        } ?: run {
            // Show loading or error state if book is null
            BookDetailsScreen(
                book = null,
                progress = null,
                review = null,
                onBackClick = { navController.popBackStack() },
                onUpdateProgress = { newProgress ->
                    viewModel.updateReadingProgress(newProgress)
                },
                onSaveReview = { newReview ->
                    viewModel.addOrUpdateReview(newReview)
                }
            )
        }
    }
}

// Extension function for cleaner navigation
fun androidx.navigation.NavHostController.navigateToDashboard() {
    navigate(Screen.Dashboard.route) {
        // Clear back stack up to and including auth screen
        popUpTo(Screen.Auth.route) { inclusive = true }
    }
}

fun androidx.navigation.NavHostController.navigateToAuth() {
    navigate(Screen.Auth.route) {
        // Clear back stack when going to auth
        popUpTo(0)
    }
}

fun androidx.navigation.NavHostController.navigateToProfile() {
    navigate(Screen.Profile.route)
}

fun androidx.navigation.NavHostController.navigateToBookDetails(bookId: String) {
    navigate(Screen.BookDetails.createRoute(bookId))
}