package com.hkm.stickhub.cloud

/** Guards the remote snapshot from being replaced by an empty local library. */
internal object CloudBackupPolicy {
    private const val EMPTY_LIBRARY_MESSAGE =
        "Cloud backup was not started because the local library is empty. Add a sticker first to protect existing cloud data."

    fun validateUpload(stickerCount: Int): String? =
        if (stickerCount <= 0) EMPTY_LIBRARY_MESSAGE else null
}
