package com.hkm.stickhub.service

/**
 * Action to take after a sticker is copied from the Quick Stickers popup.
 */
enum class OverlayAfterCopyAction(val id: String, val displayName: String) {
    CLOSE_POPUP("close_popup", "Close popup"),
    KEEP_OPEN("keep_open", "Keep popup open");

    companion object {
        fun fromId(id: String?): OverlayAfterCopyAction {
            if (id.isNullOrBlank()) return CLOSE_POPUP
            return entries.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) } ?: CLOSE_POPUP
        }
    }
}
