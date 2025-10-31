package com.johnmaronga.bookflow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.johnmaronga.bookflow.navigation.BookFlowNavigation
import com.johnmaronga.bookflow.ui.screens.WebSearchDialog
import com.johnmaronga.bookflow.ui.theme.BookFlowTheme
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookFlowApp()
        }
    }
}

@Composable
fun BookFlowApp() {
    BookFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // State for controlling the web search dialog
            var showWebSearchDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            // Show web search dialog when needed
            if (showWebSearchDialog) {
                WebSearchDialog(
                    onDismiss = { showWebSearchDialog = false },
                    onSearch = { query ->
                        openGoogleSearch(context, query)
                    }
                )
            }

            BookFlowNavigation(
                onWebSearchRequest = {
                    showWebSearchDialog = true
                }
            )
        }
    }
}

// Function to open Google search
private fun openGoogleSearch(context: Context, query: String) {
    try {
        val encodedQuery = URLEncoder.encode("books $query", "UTF-8")
        val searchUrl = "https://www.google.com/search?q=$encodedQuery"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No browser app found", Toast.LENGTH_SHORT).show()
        Log.e("WebSearch", "Error opening browser: ${e.message}")
    }
}