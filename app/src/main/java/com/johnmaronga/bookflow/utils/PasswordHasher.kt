package com.johnmaronga.bookflow.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordHasher {

    private const val SALT_LENGTH = 16

    /**
     * Hash a password with a random salt using SHA-256
     * Returns: "salt:hash" format
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashWithSalt(password, salt)
        return "${encodeToBase64(salt)}:${encodeToBase64(hash)}"
    }

    /**
     * Verify a password against a stored hash
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = decodeFromBase64(parts[0])
            val hash = decodeFromBase64(parts[1])
            val testHash = hashWithSalt(password, salt)

            hash.contentEquals(testHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    private fun hashWithSalt(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }

    private fun encodeToBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun decodeFromBase64(str: String): ByteArray {
        return Base64.getDecoder().decode(str)
    }
}
