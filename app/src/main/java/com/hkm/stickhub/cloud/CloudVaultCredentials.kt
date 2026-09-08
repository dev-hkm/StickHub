package com.hkm.stickhub.cloud

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/**
 * Anonymous cloud-vault credentials. The secret is both the client-side
 * encryption key and the proof used to authenticate with the Worker.
 * Recovery code is the only cross-device credential; it must never be sent to
 * logs or analytics.
 */
data class CloudVaultCredentials(
    val vaultId: String,
    val secret: ByteArray
) {
    init {
        require(vaultId.matches(UUID_REGEX)) { "Invalid cloud vault id" }
        require(secret.size == SECRET_BYTES) { "Cloud vault secret must be 32 bytes" }
    }

    val recoveryCode: String
        get() = "$CODE_PREFIX.$vaultId.${encode(secret)}"

    val secretHashHex: String
        get() = MessageDigest.getInstance("SHA-256")
            .digest(secret)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    companion object {
        private const val CODE_PREFIX = "SH1"
        private const val SECRET_BYTES = 32
        private val UUID_REGEX = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
        )
        private val secureRandom = SecureRandom()

        fun generate(): CloudVaultCredentials {
            val secret = ByteArray(SECRET_BYTES)
            secureRandom.nextBytes(secret)
            return CloudVaultCredentials(UUID.randomUUID().toString(), secret)
        }

        fun parseRecoveryCode(raw: String): CloudVaultCredentials {
            val parts = raw.trim().split('.')
            require(parts.size == 3 && parts[0] == CODE_PREFIX) { "Invalid recovery code" }
            val decoded = try {
                Base64.decode(parts[2], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            } catch (_: Exception) {
                throw IllegalArgumentException("Invalid recovery code",)
            }
            return try {
                CloudVaultCredentials(parts[1], decoded)
            } catch (_: Exception) {
                throw IllegalArgumentException("Invalid recovery code")
            }
        }

        private fun encode(bytes: ByteArray): String =
            Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
