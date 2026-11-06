package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtil {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = "$password:$salt"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val hash = hashPassword(password, salt)
        return hash.equals(expectedHash, ignoreCase = true)
    }
}
