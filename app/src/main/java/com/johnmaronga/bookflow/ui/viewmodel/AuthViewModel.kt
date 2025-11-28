package com.johnmaronga.bookflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.johnmaronga.bookflow.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isSignUpMode: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isFormValid: Boolean = false
)

sealed class AuthEvent {
    object SignInSuccess : AuthEvent()
    object SignUpSuccess : AuthEvent()
    object SkipAuth : AuthEvent()
    object LogoutSuccess : AuthEvent()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authEvents = MutableSharedFlow<AuthEvent>()
    val authEvents = _authEvents.asSharedFlow()

    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            val isEmailValid = isValidEmail(email)
            val isFormValid = validateForm(
                email = email,
                password = currentState.password,
                name = currentState.name,
                isSignUpMode = currentState.isSignUpMode
            )

            currentState.copy(
                email = email,
                isEmailValid = isEmailValid,
                emailError = if (!isEmailValid && email.isNotBlank()) "Please enter a valid email" else null,
                error = null,
                isFormValid = isFormValid
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            val isPasswordValid = isValidPassword(password)
            val isFormValid = validateForm(
                email = currentState.email,
                password = password,
                name = currentState.name,
                isSignUpMode = currentState.isSignUpMode
            )

            currentState.copy(
                password = password,
                isPasswordValid = isPasswordValid,
                passwordError = if (!isPasswordValid && password.isNotBlank()) "Password must be at least 6 characters" else null,
                error = null,
                isFormValid = isFormValid
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { currentState ->
            val isNameValid = name.isNotBlank() || !currentState.isSignUpMode
            val isFormValid = validateForm(
                email = currentState.email,
                password = currentState.password,
                name = name,
                isSignUpMode = currentState.isSignUpMode
            )

            currentState.copy(
                name = name,
                nameError = if (!isNameValid && currentState.isSignUpMode) "Please enter your name" else null,
                error = null,
                isFormValid = isFormValid
            )
        }
    }

    fun toggleSignUpMode() {
        _uiState.update { currentState ->
            val newSignUpMode = !currentState.isSignUpMode
            val isFormValid = validateForm(
                email = currentState.email,
                password = currentState.password,
                name = currentState.name,
                isSignUpMode = newSignUpMode
            )

            currentState.copy(
                isSignUpMode = newSignUpMode,
                isFormValid = isFormValid
            )
        }
        // Reset form errors when switching modes
        clearFormErrors()
    }

    fun clearFormErrors() {
        _uiState.update {
            it.copy(
                nameError = null,
                emailError = null,
                passwordError = null,
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun signIn() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.signIn(_uiState.value.email, _uiState.value.password).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    _authEvents.emit(AuthEvent.SignInSuccess)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Sign in failed"
                        )
                    }
                }
            )
        }
    }

    fun signUp() {
        if (!validateInputs(requireName = true)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.signUp(
                email = _uiState.value.email,
                password = _uiState.value.password,
                name = _uiState.value.name.ifBlank { null }
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                    _authEvents.emit(AuthEvent.SignUpSuccess)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Sign up failed"
                        )
                    }
                }
            )
        }
    }

    fun skipAuth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticated = true) }
            _authEvents.emit(AuthEvent.SkipAuth)
        }
    }

    private fun validateInputs(requireName: Boolean = false): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password
        val name = _uiState.value.name

        val isEmailValid = isValidEmail(email)
        val isPasswordValid = isValidPassword(password)
        val isNameValid = !requireName || name.isNotBlank()

        _uiState.update {
            it.copy(
                isEmailValid = isEmailValid,
                isPasswordValid = isPasswordValid,
                emailError = if (!isEmailValid) "Please enter a valid email address" else null,
                passwordError = if (!isPasswordValid) "Password must be at least 6 characters" else null,
                nameError = if (!isNameValid && requireName) "Please enter your name" else null,
                error = when {
                    !isEmailValid -> "Please enter a valid email address"
                    !isPasswordValid -> "Password must be at least 6 characters"
                    !isNameValid -> "Please enter your name"
                    else -> null
                }
            )
        }

        return isEmailValid && isPasswordValid && isNameValid
    }

    private fun validateForm(
        email: String,
        password: String,
        name: String,
        isSignUpMode: Boolean
    ): Boolean {
        val isEmailValid = isValidEmail(email)
        val isPasswordValid = isValidPassword(password)
        val isNameValid = !isSignUpMode || name.isNotBlank()

        return isEmailValid && isPasswordValid && isNameValid
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun resetState() {
        _uiState.update {
            AuthUiState( // Reset to default state instead of copying
                isSignUpMode = it.isSignUpMode // Preserve the sign up mode preference
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // Clear user session through repository if needed
                // authRepository.logout()

                // Reset state
                resetState()
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        isSignUpMode = false // Reset to sign in mode after logout
                    )
                }

                // Emit logout event
                _authEvents.emit(AuthEvent.LogoutSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Logout failed: ${e.message}") }
            }
        }
    }

    fun onAuthScreenLeave() {
        // Optional: You can choose to reset state or just clear errors
        clearFormErrors()
    }
}