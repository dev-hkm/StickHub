package com.hkm.stickhub.cloud

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores the vault recovery code encrypted by an Android Keystore key. */
object CloudVaultStore {
    private const val PREFS = "stickhub_cloud_vault"
    private const val ENCRYPTED_CODE = "encrypted_recovery_code"
    private const val KEY_ALIAS = "stickhub_cloud_vault_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun get(context: Context): CloudVaultCredentials? {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(ENCRYPTED_CODE, null) ?: return null
        return try {
            val parts = encoded.split('.')
            require(parts.size == 2)
            val iv = Base64.decode(parts[0], Base64.DEFAULT)
            val ciphertext = Base64.decode(parts[1], Base64.DEFAULT)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            CloudVaultCredentials.parseRecoveryCode(
                cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun save(context: Context, credentials: CloudVaultCredentials) {
        val iv = ByteArray(12)
        java.security.SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(), GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(credentials.recoveryCode.toByteArray(Charsets.UTF_8))
        val value = "${Base64.encodeToString(iv, Base64.NO_WRAP)}.${Base64.encodeToString(ciphertext, Base64.NO_WRAP)}"
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ENCRYPTED_CODE, value)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(ENCRYPTED_CODE)
            .apply()
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }
}
