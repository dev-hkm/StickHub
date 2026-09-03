package com.hkm.stickhub.util

/**
 * Pure import gate shared by clipboard UI and repository code.
 *
 * A StickHub provider URI represents a sticker the user just copied *out* of StickHub. It must
 * never be offered back as a new import candidate.
 */
object ClipboardImportPolicy {
    const val STICKER_PROVIDER_AUTHORITY = "com.hkm.stickhub.stickerprovider"

    fun isOwnStickerSource(scheme: String?, authority: String?): Boolean {
        return scheme.equals("content", ignoreCase = true) &&
            authority.equals(STICKER_PROVIDER_AUTHORITY, ignoreCase = true)
    }

    fun isEligibleImage(
        scheme: String?,
        authority: String?,
        resolvedMimeType: String?,
        declaredMimeTypes: Iterable<String>
    ): Boolean {
        if (isOwnStickerSource(scheme, authority)) return false
        if (!scheme.equals("content", ignoreCase = true) && !scheme.equals("file", ignoreCase = true)) {
            return false
        }

        return isImageMimeType(resolvedMimeType) || declaredMimeTypes.any(::isImageMimeType)
    }

    private fun isImageMimeType(mimeType: String?): Boolean {
        return mimeType?.trim()?.lowercase()?.startsWith("image/") == true
    }
}
