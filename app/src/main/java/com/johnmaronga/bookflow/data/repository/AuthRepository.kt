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

    // Keep only the flows that are actually used
    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    /**
     * Sign up a new user
     * Returns: Result with Unit on success, error message on failure
     */
    suspend fun signUp(email: String, password: String, name: String? = null): Result<Unit> {
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

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in an existing user
     * Returns: Result with Unit on success, error message on failure
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
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

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}