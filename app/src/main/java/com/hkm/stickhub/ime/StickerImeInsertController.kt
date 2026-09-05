package com.hkm.stickhub.ime

import android.net.Uri
import android.view.inputmethod.InputConnection
import com.hkm.stickhub.util.StickerExportService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Pure insert orchestration for StickHub Keyboard. All Android-framework
 * touching lives behind [Gateway] so this decision table is exactly
 * unit-testable with fakes — no emulator, no real editor needed.
 *
 * Honest contract, stated once: [insertSticker] attempts a single Commit
 * Content insert when the focused editor advertises a compatible image MIME.
 * Anything else — unsupported editor, null connection, rejected commit,
 * denied permission, unreadable payload, pre-25 platform — falls back to
 * clipboard exactly once and never duplicates. Whether the target renders a
 * compact native sticker or a large photo stays the target app's decision;
 * this controller only guarantees a correct handoff, never a rendering.
 */
class StickerImeInsertController(private val gateway: Gateway) {

    interface Gateway {
        fun sdkInt(): Int
        /** MIME types from EditorInfoCompat, or null when unknown. */
        fun editorContentMimes(): Array<String>?
        fun inputConnection(): InputConnection?
        /**
         * Wraps InputConnectionCompat.commitContent with the read-grant flag.
         * Returns true only when the editor accepted the content.
         */
        fun commitImage(connection: InputConnection, uri: Uri, mimeType: String): Boolean
        /** Clipboard fallback. Returns true when the clip was published. */
        fun fallbackCopy(uri: Uri, mimeType: String): Boolean
    }

    enum class CommitFailure {
        NO_PAYLOAD,
        UNSUPPORTED_PLATFORM,
        UNSUPPORTED_EDITOR,
        NO_CONNECTION,
        COMMIT_FAILED,
        PERMISSION_DENIED,
        FALLBACK_FAILED
    }

    data class InsertOutcome(
        val committed: Boolean,
        val fallbackUsed: Boolean,
        val failure: CommitFailure?,
        val ignoredDueToInflight: Boolean = false
    )

    private val inFlight = AtomicBoolean(false)

    companion object {
        /** Commit Content compat handshake needs API 25+; below that, clipboard. */
        const val MIN_COMMIT_SDK = 25

        /**
         * True when the editor explicitly advertises PNG or wildcard image
         * rich content. Anything else (including null) means clipboard.
         */
        fun editorSupportsImage(mimes: Array<String>?): Boolean {
            if (mimes == null) return false
            return mimes.any {
                it.equals("image/png", ignoreCase = true) ||
                    it.equals("image/*", ignoreCase = true)
            }
        }
    }

    fun insertSticker(payload: StickerExportService.ExportPayload?): InsertOutcome {
        if (!inFlight.compareAndSet(false, true)) {
            return InsertOutcome(
                committed = false,
                fallbackUsed = false,
                failure = null,
                ignoredDueToInflight = true
            )
        }
        try {
            if (payload == null) {
                return InsertOutcome(false, false, CommitFailure.NO_PAYLOAD)
            }
            if (gateway.sdkInt() < MIN_COMMIT_SDK) {
                return fallbackOnce(payload, CommitFailure.UNSUPPORTED_PLATFORM)
            }
            if (!editorSupportsImage(gateway.editorContentMimes())) {
                return fallbackOnce(payload, CommitFailure.UNSUPPORTED_EDITOR)
            }
            val connection = gateway.inputConnection()
                ?: return fallbackOnce(payload, CommitFailure.NO_CONNECTION)
            val accepted = try {
                gateway.commitImage(connection, payload.uri, payload.mimeType)
            } catch (security: SecurityException) {
                return fallbackOnce(payload, CommitFailure.PERMISSION_DENIED)
            } catch (_: Exception) {
                return fallbackOnce(payload, CommitFailure.COMMIT_FAILED)
            }
            if (!accepted) {
                return fallbackOnce(payload, CommitFailure.COMMIT_FAILED)
            }
            return InsertOutcome(committed = true, fallbackUsed = false, failure = null)
        } finally {
            inFlight.set(false)
        }
    }

    private fun fallbackOnce(
        payload: StickerExportService.ExportPayload,
        reason: CommitFailure
    ): InsertOutcome {
        val copied = try {
            gateway.fallbackCopy(payload.uri, payload.mimeType)
        } catch (_: Exception) {
            false
        }
        return if (copied) {
            InsertOutcome(committed = false, fallbackUsed = true, failure = reason)
        } else {
            InsertOutcome(committed = false, fallbackUsed = false, failure = CommitFailure.FALLBACK_FAILED)
        }
    }
}
