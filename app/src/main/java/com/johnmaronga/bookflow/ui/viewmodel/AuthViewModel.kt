package com.johnmaronga.bookflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isAuthenticated: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            isEmailValid = true,
            error = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            isPasswordValid = true,
            error = null
        )
    }

    fun signIn() {
        if (!validateInputs()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // TODO: Implement actual authentication logic
        // For now, simulate successful sign in
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isAuthenticated = true
        )
    }

    fun signUp() {
        if (!validateInputs()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // TODO: Implement actual sign up logic
        // For now, simulate successful sign up
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isAuthenticated = true
        )
    }

    fun skipAuth() {
        _uiState.value = _uiState.value.copy(isAuthenticated = true)
    }

    private fun validateInputs(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val isEmailValid = isValidEmail(email)
        val isPasswordValid = isValidPassword(password)

        _uiState.value = _uiState.value.copy(
            isEmailValid = isEmailValid,
            isPasswordValid = isPasswordValid,
            error = when {
                !isEmailValid -> "Please enter a valid email address"
                !isPasswordValid -> "Password must be at least 6 characters"
                else -> null
            }
        )

        return isEmailValid && isPasswordValid
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
