package com.hkm.stickhub.cloud

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** SHB2: independently authenticated 1 MiB frames; memory is independent of file size. */
internal object CloudBackupStream {
    private const val CHUNK = 1024 * 1024
    private val MAGIC = byteArrayOf(83, 72, 66, 50)

    suspend fun encrypt(source: File, destination: File, credentials: CloudVaultCredentials) {
        val prefix = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val header = ByteBuffer.allocate(32).put(MAGIC).put(prefix).putInt(CHUNK).putLong(source.length()).array()
        DataInputStream(source.inputStream().buffered()).use { input ->
            DataOutputStream(destination.outputStream().buffered()).use { output ->
                output.write(header)
                var remaining = source.length()
                var index = 0
                val buffer = ByteArray(CHUNK)
                while (remaining > 0) {
                    currentCoroutineContext().ensureActive()
                    val size = minOf(CHUNK.toLong(), remaining).toInt()
                    input.readFully(buffer, 0, size)
                    val encrypted = cipher(Cipher.ENCRYPT_MODE, credentials, prefix, index++, header).doFinal(buffer, 0, size)
                    output.write(encrypted)
                    remaining -= size
                }
                check(input.read() == -1) { "Backup changed during encryption." }
                // Authenticated end marker detects truncation, including an empty archive.
                output.write(cipher(Cipher.ENCRYPT_MODE, credentials, prefix, index, header).doFinal())
            }
        }
    }

    suspend fun decrypt(source: File, destination: File, credentials: CloudVaultCredentials) {
        DataInputStream(source.inputStream().buffered()).use { input ->
            val magic = ByteArray(4).also { input.readFully(it) }
            if (magic.contentEquals(byteArrayOf(83, 72, 66, 49))) {
                // Legacy SHB1 was limited to 64 MiB and used one GCM message.
                check(source.length() <= 64L * 1024 * 1024) { "Invalid legacy backup size." }
                destination.writeBytes(CloudBackupCrypto.decrypt(source.readBytes(), credentials))
                return
            }
            check(magic.contentEquals(MAGIC)) { "Unsupported cloud backup format." }
            val prefix = ByteArray(16).also { input.readFully(it) }
            val chunk = input.readInt()
            val size = input.readLong()
            check(chunk == CHUNK && size >= 0 && size <= 5L * 1024 * 1024 * 1024 * 1024) { "Invalid cloud backup header." }
            val frames = (size + CHUNK - 1) / CHUNK
            check(source.length() == 32L + size + (frames + 1) * 16) { "Cloud backup is truncated." }
            val header = ByteBuffer.allocate(32).put(magic).put(prefix).putInt(chunk).putLong(size).array()
            destination.outputStream().buffered().use { output ->
                var remaining = size
                var index = 0
                while (remaining > 0) {
                    currentCoroutineContext().ensureActive()
                    val count = minOf(CHUNK.toLong(), remaining).toInt()
                    val encrypted = ByteArray(count + 16).also { input.readFully(it) }
                    output.write(cipher(Cipher.DECRYPT_MODE, credentials, prefix, index++, header).doFinal(encrypted))
                    remaining -= count
                }
                cipher(Cipher.DECRYPT_MODE, credentials, prefix, index, header).doFinal(ByteArray(16).also { input.readFully(it) })
                check(input.read() == -1) { "Unexpected cloud backup data." }
            }
        }
    }

    private fun cipher(mode: Int, credentials: CloudVaultCredentials, prefix: ByteArray, index: Int, header: ByteArray): Cipher =
        Cipher.getInstance("AES/GCM/NoPadding").apply {
            val fileKey = Mac.getInstance("HmacSHA256").run {
                init(SecretKeySpec(credentials.secret, "HmacSHA256"))
                update(MAGIC)
                doFinal(prefix)
            }
            init(mode, SecretKeySpec(fileKey, "AES"), GCMParameterSpec(128, ByteBuffer.allocate(12).putLong(0).putInt(index).array()))
            updateAAD(header)
        }
}
