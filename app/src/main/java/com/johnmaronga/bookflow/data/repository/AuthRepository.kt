package com.johnmaronga.bookflow.data.repository

import com.johnmaronga.bookflow.data.local.SessionManager
import com.johnmaronga.bookflow.data.local.dao.UserDao
import com.johnmaronga.bookflow.data.local.entity.UserEntity
import com.johnmaronga.bookflow.utils.PasswordHasher
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {

    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn
    val currentUserEmail: Flow<String?> = sessionManager.userEmail

    /**
     * Sign up a new user
     * Returns: Result with user ID on success, error message on failure
     */
    suspend fun signUp(email: String, password: String, name: String? = null): Result<Long> {
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("An account with this email already exists"))
            }

            // Hash password and create user
            val passwordHash = PasswordHasher.hashPassword(password)
            val user = UserEntity(
                email = email,
                passwordHash = passwordHash,
                name = name
            )

            val userId = userDao.insertUser(user)

            // Save session
            sessionManager.saveSession(userId, email, name)

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in an existing user
     * Returns: Result with user ID on success, error message on failure
     */
    suspend fun signIn(email: String, password: String): Result<Long> {
        return try {
            val user = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("No account found with this email"))

            // Verify password
            if (!PasswordHasher.verifyPassword(password, user.passwordHash)) {
                return Result.failure(Exception("Incorrect password"))
            }

            // Update last login
            userDao.updateLastLogin(user.id)

            // Save session
            sessionManager.saveSession(user.id, user.email, user.name)

            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user
     */
    suspend fun signOut() {
        sessionManager.clearSession()
    }

    /**
     * Get current user
     */
    suspend fun getCurrentUser(): UserEntity? {
        val userId = sessionManager.getCurrentUserId() ?: return null
        return userDao.getUserById(userId)
    }

    /**
     * Check if any users exist (for first-time setup)
     */
    suspend fun hasAnyUsers(): Boolean {
        return userDao.getUserCount() > 0
    }
}
