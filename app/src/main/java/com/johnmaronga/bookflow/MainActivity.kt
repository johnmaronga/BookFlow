package com.johnmaronga.bookflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.johnmaronga.bookflow.navigation.BookFlowNavigation
import com.johnmaronga.bookflow.ui.screens.WebSearchDialog
import com.johnmaronga.bookflow.ui.theme.BookFlowTheme
import java.net.URLEncoder
import androidx.core.net.toUri

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
            val context = LocalContext.current
            val musicApp = context.applicationContext as Music
            var isMuted by remember { mutableStateOf(musicApp.isMuted()) }

            // State for controlling the web search dialog
            var showWebSearchDialog by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxSize()) {
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

                // Add floating audio toggle button
                FloatingActionButton(
                    onClick = {
                        musicApp.toggleMusic()
                        isMuted = musicApp.isMuted()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (isMuted) {
                        Text("🔇", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("🔊", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

// Function to open Google search
private fun openGoogleSearch(context: Context, query: String) {
    try {
        val encodedQuery = URLEncoder.encode("books $query", "UTF-8")
        val searchUrl = "https://www.google.com/search?q=$encodedQuery"
        val intent = Intent(Intent.ACTION_VIEW, searchUrl.toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No browser app found", Toast.LENGTH_SHORT).show()
        Log.e("WebSearch", "Error opening browser: ${e.message}")
    }
}