package com.hkm.stickhub

import com.hkm.stickhub.util.ClipboardContentHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class ClipboardContentHasherTest {

    @Test
    fun sameBytesProduceTheSameStableSha256Digest() {
        val first = ClipboardContentHasher.sha256(ByteArrayInputStream("same sticker".encodeToByteArray()))
        val second = ClipboardContentHasher.sha256(ByteArrayInputStream("same sticker".encodeToByteArray()))

        assertEquals(first, second)
        assertEquals(64, first.length)
    }

    @Test
    fun differentBytesDoNotProduceTheSameDigest() {
        val first = ClipboardContentHasher.sha256(ByteArrayInputStream("first sticker".encodeToByteArray()))
        val second = ClipboardContentHasher.sha256(ByteArrayInputStream("second sticker".encodeToByteArray()))

        assertNotEquals(first, second)
    }
}
