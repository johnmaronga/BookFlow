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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.johnmaronga.bookflow.data.local.AppDatabase
import com.johnmaronga.bookflow.data.local.SessionManager
import com.johnmaronga.bookflow.data.remote.RetrofitClient
import com.johnmaronga.bookflow.data.repository.BookRepositoryImpl
import com.johnmaronga.bookflow.ui.screens.AuthScreen
import com.johnmaronga.bookflow.ui.screens.BookDetailsScreen
import com.johnmaronga.bookflow.ui.screens.DashboardScreen
import com.johnmaronga.bookflow.ui.screens.WelcomeScreen
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
}

@Composable
fun BookFlowNavigation(
    onWebSearchRequest: () -> Unit = {} // Add this parameter
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = {
                    // Navigate to Auth screen when "Let's Get Started" is clicked
                    navController.navigate(Screen.Auth.route)
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onSignInClick = {
                    // TODO: Navigate to Sign In process or Home screen
                    // navController.navigate(Screen.Home.route)
                    // For now, navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    // TODO: Navigate to Sign Up process
                    // navController.navigate(Screen.SignUp.route)
                    // For now, navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    // Go back to Welcome screen
                    navController.popBackStack()
                },
                onSkipForNowClick = {
                    // Navigate to Dashboard screen when "Skip for now" is clicked
                    navController.navigate(Screen.Dashboard.route) {
                        // Remove auth screen from back stack so user can't go back
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onSeeAllClick = { section ->
                    // TODO: Navigate to detailed lists when implemented
                },
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                },
                onWebSearchClick = onWebSearchRequest
            )
        }

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
            }
        }

        // You can add more composable destinations here for future screens:
        /*
        composable(Screen.AddBook.route) {
            AddBookScreen(
                onBackClick = { navController.popBackStack() },
                onBookAdded = { book ->
                    navController.navigate("${Screen.BookDetails.route}/${book.id}")
                }
            )
        }

        composable("${Screen.BookDetails.route}/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(
                bookId = bookId,
                onBackClick = { navController.popBackStack() },
                onEditClick = {
                    // Navigate to edit screen
                }
            )
        }
        */
    }
}