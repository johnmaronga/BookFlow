package com.johnmaronga.bookflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.johnmaronga.bookflow.ui.theme.BookFlowTheme
import com.johnmaronga.bookflow.ui.viewmodel.AuthEvent
import com.johnmaronga.bookflow.ui.viewmodel.AuthViewModel
import com.johnmaronga.bookflow.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSkipForNowClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Handle authentication events
    LaunchedEffect(Unit) {
        viewModel.authEvents.collectLatest { event ->
            when (event) {
                AuthEvent.SignInSuccess -> {  // Remove AuthViewModel. prefix
                    snackbarHostState.showSnackbar("Welcome back!")
                    viewModel.resetState()
                    onSignInSuccess()
                }
                AuthEvent.SignUpSuccess -> {  // Remove AuthViewModel. prefix
                    snackbarHostState.showSnackbar("Account created successfully!")
                    viewModel.resetState()
                    onSignUpSuccess()
                }
                AuthEvent.SkipAuth -> {  // Remove AuthViewModel. prefix
                    snackbarHostState.showSnackbar("Continuing as guest")
                    viewModel.resetState()
                    onSkipForNowClick()
                }
                AuthEvent.LogoutSuccess -> {  // Remove AuthViewModel. prefix
                    // Handled elsewhere
                }
            }
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Text(
                    text = "Welcome to Book Flow",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (uiState.isSignUpMode)
                        "Create your account to start your reading journey"
                    else
                        "Sign in to continue your reading journey",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Name field (only for sign up)
                if (uiState.isSignUpMode) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        isError = uiState.nameError != null,
                        supportingText = {
                            uiState.nameError?.let { error ->
                                Text(text = error, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Email field
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.emailError != null,
                    enabled = !uiState.isLoading,
                    supportingText = {
                        uiState.emailError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.passwordError != null,
                    enabled = !uiState.isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    supportingText = {
                        uiState.passwordError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    )
                )

                // Password requirements hint (only show during sign up)
                if (uiState.isSignUpMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Password must be at least 6 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Main action button (Sign In or Sign Up)
                Button(
                    onClick = {
                        if (uiState.isSignUpMode) {
                            viewModel.signUp()
                        } else {
                            viewModel.signIn()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && uiState.isFormValid
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = if (uiState.isSignUpMode) "Create Account" else "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle between Sign In and Sign Up
                TextButton(
                    onClick = {
                        viewModel.toggleSignUpMode()
                        viewModel.clearFormErrors()
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = if (uiState.isSignUpMode)
                            "Already have an account? Sign In"
                        else
                            "Don't have an account? Sign Up",
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Back Button
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text("Back to Welcome")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip for Now Button
                Button(
                    onClick = {
                        viewModel.skipAuth()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Skip for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    BookFlowTheme {
        AuthScreen(
            onSignInSuccess = {},
            onSignUpSuccess = {},
            onBackClick = {},
            onSkipForNowClick = {}
        )
    }
}