package com.johnmaronga.bookflow

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.johnmaronga.bookflow.navigation.BookFlowNavigation
import com.johnmaronga.bookflow.ui.screens.WebSearchDialog
import com.johnmaronga.bookflow.ui.theme.BookFlowTheme
import com.johnmaronga.bookflow.utils.PermissionHelper
import com.johnmaronga.bookflow.workers.WorkManagerScheduler
import java.net.URLEncoder

lateinit var content: () -> Unit

class MainActivity : ComponentActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private var hasRequestedPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup permission launcher
        notificationPermissionLauncher = PermissionHelper.createNotificationPermissionLauncher(this) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                // Enable reading reminders
                WorkManagerScheduler.scheduleReadingReminders(this)
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            BookFlowApp(
                onRequestNotificationPermission = {
                    requestNotificationPermission()
                }
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this) && !hasRequestedPermission) {
                hasRequestedPermission = true
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun BookFlowApp(
    onRequestNotificationPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Check and request notification permission on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(context)) {
                showPermissionDialog = true
            }
        }
    }

    BookFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val musicApp = context.applicationContext as Music
            var isMuted by remember { mutableStateOf(musicApp.isMuted()) }

            // State for controlling the web search dialog
            var showWebSearchDialog by remember { mutableStateOf(false) }

            // Permission request dialog
            if (showPermissionDialog) {
                PermissionRequestDialog(
                    onDismiss = { },
                    onConfirm = {
                        onRequestNotificationPermission()
                    }
                )
            }

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

@Composable
fun PermissionRequestDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Enable Notifications")
        },
        text = {
            Text("BookFlow would like to send you reading reminders to help you stay on track with your reading goals.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
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

