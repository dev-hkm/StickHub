package com.hkm.stickhub

import com.hkm.stickhub.util.ClipboardImportPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardImportPolicyTest {

    @Test
    fun ownStickerProviderIsNeverEligibleForClipboardImport() {
        assertTrue(
            ClipboardImportPolicy.isOwnStickerSource(
                scheme = "content",
                authority = "com.hkm.stickhub.stickerprovider"
            )
        )
        assertFalse(
            ClipboardImportPolicy.isEligibleImage(
                scheme = "content",
                authority = "com.hkm.stickhub.stickerprovider",
                resolvedMimeType = "image/png",
                declaredMimeTypes = listOf("image/png")
            )
        )
    }

    @Test
    fun externalImageIsEligibleButArbitraryContentUriIsNot() {
        assertTrue(
            ClipboardImportPolicy.isEligibleImage(
                scheme = "content",
                authority = "com.google.android.apps.photos.content",
                resolvedMimeType = "image/png",
                declaredMimeTypes = emptyList()
            )
        )
        assertFalse(
            ClipboardImportPolicy.isEligibleImage(
                scheme = "content",
                authority = "external.documents",
                resolvedMimeType = "application/pdf",
                declaredMimeTypes = emptyList()
            )
        )
        assertFalse(
            ClipboardImportPolicy.isEligibleImage(
                scheme = "content",
                authority = "external.documents",
                resolvedMimeType = null,
                declaredMimeTypes = emptyList()
            )
        )
    }

    @Test
    fun wildcardClipboardImageMimeIsAcceptedForExternalSource() {
        assertTrue(
            ClipboardImportPolicy.isEligibleImage(
                scheme = "content",
                authority = "external.provider",
                resolvedMimeType = null,
                declaredMimeTypes = listOf("image/*")
            )
        )
    }
}
