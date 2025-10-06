package com.std.sol.utils

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {

    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasUpperCase && hasDigit && hasSpecialChar
    }

    fun hashPassword(password: String, salt: String = generateSalt()): String {
        val saltedPassword = password + salt
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword.toByteArray())
        return salt + hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        val salt = hashedPassword.substring(0, 32)
        val hash = hashPassword(password, salt)
        return hash == hashedPassword
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }


}
