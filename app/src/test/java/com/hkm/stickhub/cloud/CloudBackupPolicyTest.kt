package com.hkm.stickhub.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CloudBackupPolicyTest {

    @Test
    fun emptyLibraryIsRejectedBeforeItCanOverwriteACloudSnapshot() {
        assertEquals(
            "Cloud backup was not started because the local library is empty. Add a sticker first to protect existing cloud data.",
            CloudBackupPolicy.validateUpload(stickerCount = 0)
        )
    }

    @Test
    fun nonEmptyLibraryIsAllowedToCreateOrReplaceSnapshot() {
        assertNull(CloudBackupPolicy.validateUpload(stickerCount = 1))
    }
}
