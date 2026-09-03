package com.hkm.stickhub

import com.hkm.stickhub.util.BackupHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSafetyTest {

    @Test
    fun testSafeZipPaths() {
        assertTrue(BackupHelper.isSafeZipPath("metadata.json"))
        assertTrue(BackupHelper.isSafeZipPath("stickers/cat.png"))
        assertTrue(BackupHelper.isSafeZipPath("stickers/1700000000.webp"))
    }

    @Test
    fun testRejectZipSlipPaths() {
        assertFalse(BackupHelper.isSafeZipPath("../metadata.json"))
        assertFalse(BackupHelper.isSafeZipPath("../../etc/passwd"))
        assertFalse(BackupHelper.isSafeZipPath("stickers/../../evil.sh"))
        assertFalse(BackupHelper.isSafeZipPath("/root/file.png"))
        assertFalse(BackupHelper.isSafeZipPath("\\Windows\\System32\\cmd.exe"))
        assertFalse(BackupHelper.isSafeZipPath(""))
    }

    @Test
    fun testBackupStickerBasenames() {
        assertTrue(BackupHelper.isValidBasename("cat_123.png"))
        assertTrue(BackupHelper.isValidBasename("meme-sticker.webp"))
        assertTrue(BackupHelper.isValidBasename("photo.cutout.12345.png"))

        assertFalse(BackupHelper.isValidBasename("../cat.png"))
        assertFalse(BackupHelper.isValidBasename("folder/cat.png"))
        assertFalse(BackupHelper.isValidBasename("cat;rm.png"))
        assertFalse(BackupHelper.isValidBasename("cat test.png"))
    }
}
