package com.hkm.stickhub

import com.hkm.stickhub.data.provider.StickerContentProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderSecurityTest {

    @Test
    fun testValidBasenames() {
        assertTrue(StickerContentProvider.isValidBasename("sticker_1700000.png"))
        assertTrue(StickerContentProvider.isValidBasename("cat-cutout.webp"))
        assertTrue(StickerContentProvider.isValidBasename("cutout.12345.png"))
        assertTrue(StickerContentProvider.isValidBasename("a.png"))
    }

    @Test
    fun testRejectPathTraversal() {
        assertFalse(StickerContentProvider.isValidBasename("../evil.png"))
        assertFalse(StickerContentProvider.isValidBasename("..\\evil.png"))
        assertFalse(StickerContentProvider.isValidBasename("sub/evil.png"))
        assertFalse(StickerContentProvider.isValidBasename("sub\\evil.png"))
        assertFalse(StickerContentProvider.isValidBasename("../../databases/stickhub.db"))
    }

    @Test
    fun testRejectDangerousCharacters() {
        assertFalse(StickerContentProvider.isValidBasename("sticker;rm.png"))
        assertFalse(StickerContentProvider.isValidBasename("sticker|calc.png"))
        assertFalse(StickerContentProvider.isValidBasename("sticker\u0000.png"))
        assertFalse(StickerContentProvider.isValidBasename("sticker name with spaces.png"))
        assertFalse(StickerContentProvider.isValidBasename(""))
    }

    @Test
    fun testRejectExcessiveLength() {
        val longName = "a".repeat(129) + ".png"
        assertFalse(StickerContentProvider.isValidBasename(longName))
    }
}
