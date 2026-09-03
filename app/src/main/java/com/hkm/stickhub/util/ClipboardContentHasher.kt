package com.hkm.stickhub.util

import java.io.InputStream
import java.security.MessageDigest

/** SHA-256 for the exact source bytes of a clipboard import. */
object ClipboardContentHasher {
    fun sha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = inputStream.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    fun sha256(bytes: ByteArray): String = sha256(bytes.inputStream())
}
