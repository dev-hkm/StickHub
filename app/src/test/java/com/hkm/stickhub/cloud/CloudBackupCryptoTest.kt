package com.hkm.stickhub.cloud

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CloudBackupCryptoTest {
    @Test
    fun encryptedPayloadRoundTripsWithVaultSecret() {
        val credentials = CloudVaultCredentials.generate()
        val plaintext = "stickhub-backup".toByteArray()

        val encrypted = CloudBackupCrypto.encrypt(plaintext, credentials)
        val decrypted = CloudBackupCrypto.decrypt(encrypted, credentials)

        assertArrayEquals(plaintext, decrypted)
        assertNotEquals(String(plaintext), String(encrypted))
    }

    @Test
    fun encryptingSamePayloadTwiceUsesDifferentNonce() {
        val credentials = CloudVaultCredentials.generate()
        val plaintext = ByteArray(128) { it.toByte() }

        val first = CloudBackupCrypto.encrypt(plaintext, credentials)
        val second = CloudBackupCrypto.encrypt(plaintext, credentials)

        assertNotEquals(String(first), String(second))
    }

    @Test
    fun tamperedPayloadCannotBeDecrypted() {
        val credentials = CloudVaultCredentials.generate()
        val encrypted = CloudBackupCrypto.encrypt("safe".toByteArray(), credentials)
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 0x01).toByte()

        assertThrows(Exception::class.java) {
            CloudBackupCrypto.decrypt(encrypted, credentials)
        }
    }
}
