package com.hkm.stickhub.cloud

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Client-side authenticated encryption for cloud backup blobs. */
object CloudBackupCrypto {
    private val secureRandom = SecureRandom()
    private const val MAGIC = "SHB1"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun encrypt(plaintext: ByteArray, credentials: CloudVaultCredentials): ByteArray {
        val iv = ByteArray(IV_BYTES)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(credentials), GCMParameterSpec(TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext)
        return MAGIC.toByteArray(Charsets.US_ASCII) + iv + ciphertext
    }

    fun decrypt(payload: ByteArray, credentials: CloudVaultCredentials): ByteArray {
        require(payload.size > MAGIC.length + IV_BYTES + 16) { "Cloud backup payload is too short" }
        require(payload.copyOfRange(0, MAGIC.length).toString(Charsets.US_ASCII) == MAGIC) {
            "Unsupported cloud backup payload"
        }
        val ivStart = MAGIC.length
        val iv = payload.copyOfRange(ivStart, ivStart + IV_BYTES)
        val ciphertext = payload.copyOfRange(ivStart + IV_BYTES, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(credentials), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun key(credentials: CloudVaultCredentials) =
        SecretKeySpec(credentials.secret, "AES")
}
