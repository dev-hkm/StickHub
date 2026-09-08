package com.hkm.stickhub.cloud

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Small authenticated envelope for the locally stored recovery code.
 *
 * Encryption deliberately lets the provider generate the IV. Android Keystore
 * keys configured with randomized encryption reject caller-provided IVs.
 */
internal object CloudVaultEnvelope {
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun encrypt(key: SecretKey, plaintext: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv ?: error("GCM provider did not return an IV")
        require(iv.size == IV_BYTES) { "Unexpected GCM IV size" }
        val ciphertext = cipher.doFinal(plaintext)
        return "${encode(iv)}.${encode(ciphertext)}"
    }

    fun decrypt(key: SecretKey, encoded: String): ByteArray {
        val parts = encoded.split('.')
        require(parts.size == 2) { "Invalid encrypted cloud vault value" }
        val iv = decode(parts[0])
        require(iv.size == IV_BYTES) { "Invalid GCM IV size" }
        val ciphertext = decode(parts[1])
        require(ciphertext.size > TAG_BITS / 8) { "Invalid encrypted cloud vault value" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray =
        Base64.decode(value, Base64.DEFAULT)
}
