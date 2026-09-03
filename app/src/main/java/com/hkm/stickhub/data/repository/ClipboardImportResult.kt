package com.hkm.stickhub.data.repository

import com.hkm.stickhub.data.model.StickerItem

/** Result of the legacy quick-import path for a ready-made clipboard sticker. */
sealed interface ClipboardImportResult {
    data class Saved(val sticker: StickerItem) : ClipboardImportResult
    data class Duplicate(val existingSticker: StickerItem) : ClipboardImportResult
    data object OwnSource : ClipboardImportResult
    data class InvalidSource(val reason: String) : ClipboardImportResult
    data class Failed(val reason: String) : ClipboardImportResult
}
