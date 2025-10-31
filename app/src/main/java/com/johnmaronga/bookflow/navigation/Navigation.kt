package com.johnmaronga.bookflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.johnmaronga.bookflow.ui.screens.AuthScreen
import com.johnmaronga.bookflow.ui.screens.DashboardScreen
import com.johnmaronga.bookflow.ui.screens.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    // You can add more screens here later like:
    // object Home : Screen("home")
    // object SignUp : Screen("signup")
    // object AddBook : Screen("add_book")
    // object BookDetails : Screen("book_details")
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
                onAddBookClick = {
                    // TODO: Navigate to Add Book screen when implemented
                    // navController.navigate(Screen.AddBook.route)
                },
                onSeeAllClick = { section ->
                    // TODO: Navigate to detailed lists when implemented
                    // when(section) {
                    //     "currently_reading" -> navController.navigate("currently_reading_list")
                    //     "recommendations" -> navController.navigate("recommendations_list")
                    //     "reviews" -> navController.navigate("reviews_list")
                    // }
                },
                onBookClick = { bookId ->
                    // TODO: Navigate to Book Details screen when implemented
                    // navController.navigate("${Screen.BookDetails.route}/$bookId")
                },
                onWebSearchClick = onWebSearchRequest // Add the web search callback here
            )
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