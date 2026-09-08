package com.hkm.stickhub.cloud

import android.util.Base64
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CloudVaultEnvelopeTest {
    @Test
    fun encryptionGeneratesAndPersistsItsOwnGcmIv() {
        val key = SecretKeySpec(ByteArray(32) { (it + 1).toByte() }, "AES")
        val plaintext = "SH1.recovery-code".toByteArray()

        val encoded = CloudVaultEnvelope.encrypt(key, plaintext)
        val parts = encoded.split('.')

        assertEquals(2, parts.size)
        assertEquals(12, Base64.decode(parts[0], Base64.DEFAULT).size)
        assertArrayEquals(plaintext, CloudVaultEnvelope.decrypt(key, encoded))
    }

    @Test
    fun tamperedEnvelopeIsRejected() {
        val key = SecretKeySpec(ByteArray(32) { 7 }, "AES")
        val encoded = CloudVaultEnvelope.encrypt(key, "safe".toByteArray())
        val parts = encoded.split('.')
        val ciphertext = Base64.decode(parts[1], Base64.DEFAULT)
        ciphertext[ciphertext.lastIndex] = (ciphertext.last().toInt() xor 1).toByte()
        val tampered = "${parts[0]}.${Base64.encodeToString(ciphertext, Base64.NO_WRAP)}"

        assertThrows(Exception::class.java) {
            CloudVaultEnvelope.decrypt(key, tampered)
        }
    }
}
