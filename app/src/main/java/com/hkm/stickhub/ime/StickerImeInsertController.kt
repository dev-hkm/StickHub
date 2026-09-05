package com.hkm.stickhub.ime

import android.net.Uri
import android.view.inputmethod.InputConnection
import com.hkm.stickhub.util.StickerExportService
import java.util.concurrent.ConcurrentHashMap

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

    data class InputSessionSnapshot(
        val sessionId: Long,
        val packageName: String? = null,
        val fieldId: Int = 0,
        val inputType: Int = 0,
        val imeOptions: Int = 0,
        val acceptedMimes: Array<String>? = null,
        val connection: InputConnection? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as InputSessionSnapshot
            if (sessionId != other.sessionId) return false
            if (packageName != other.packageName) return false
            if (fieldId != other.fieldId) return false
            if (inputType != other.inputType) return false
            if (imeOptions != other.imeOptions) return false
            if (acceptedMimes != null) {
                if (other.acceptedMimes == null) return false
                if (!acceptedMimes.contentEquals(other.acceptedMimes)) return false
            } else if (other.acceptedMimes != null) return false
            if (connection != other.connection) return false
            return true
        }

        override fun hashCode(): Int {
            var result = sessionId.hashCode()
            result = 31 * result + (packageName?.hashCode() ?: 0)
            result = 31 * result + fieldId
            result = 31 * result + inputType
            result = 31 * result + imeOptions
            result = 31 * result + (acceptedMimes?.contentHashCode() ?: 0)
            result = 31 * result + (connection?.hashCode() ?: 0)
            return result
        }
    }

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
        /** Current input session snapshot, or null if no active session. */
        fun currentSession(): InputSessionSnapshot? = null
        /** True if the host IME service is alive and not destroyed. */
        fun isServiceAlive(): Boolean = true
    }

    enum class CommitFailure {
        NO_PAYLOAD,
        UNSUPPORTED_PLATFORM,
        UNSUPPORTED_EDITOR,
        NO_CONNECTION,
        COMMIT_FAILED,
        PERMISSION_DENIED,
        ILLEGAL_ARGUMENT,
        FALLBACK_FAILED,
        STALE_SESSION,
        SERVICE_DESTROYED,
        CANCELLED
    }

    data class InsertOutcome(
        val committed: Boolean,
        val fallbackUsed: Boolean,
        val failure: CommitFailure?,
        val ignoredDueToInflight: Boolean = false,
        val stale: Boolean = false
    )

    private val inFlightKeys = ConcurrentHashMap.newKeySet<String>()

    companion object {
        /** Commit Content compat handshake needs API 25+; below that, clipboard. */
        const val MIN_COMMIT_SDK = 25

        /**
         * True when the editor explicitly advertises PNG or wildcard image
         * rich content (case-insensitive, trimmed). Anything else (including
         * null, JPEG, fake sticker MIME) means clipboard fallback.
         */
        fun editorSupportsImage(mimes: Array<String>?): Boolean {
            if (mimes == null) return false
            return mimes.any { raw ->
                val trimmed = raw.trim().lowercase()
                trimmed == "image/png" || trimmed == "image/*"
            }
        }
    }

    fun isStickerInFlight(key: String): Boolean = inFlightKeys.contains(key)

    fun insertSticker(
        payload: StickerExportService.ExportPayload?,
        session: InputSessionSnapshot? = null
    ): InsertOutcome {
        if (!gateway.isServiceAlive()) {
            return InsertOutcome(
                committed = false,
                fallbackUsed = false,
                failure = CommitFailure.SERVICE_DESTROYED
            )
        }
        if (payload == null) {
            return InsertOutcome(false, false, CommitFailure.NO_PAYLOAD)
        }
        val inFlightKey = payload.sourceIdentity.ifBlank { payload.uri.toString() }
        if (!inFlightKeys.add(inFlightKey)) {
            return InsertOutcome(
                committed = false,
                fallbackUsed = false,
                failure = null,
                ignoredDueToInflight = true
            )
        }
        try {
            if (!gateway.isServiceAlive()) {
                return InsertOutcome(
                    committed = false,
                    fallbackUsed = false,
                    failure = CommitFailure.SERVICE_DESTROYED
                )
            }
            if (session != null) {
                val current = gateway.currentSession()
                if (current != null && current.sessionId != session.sessionId) {
                    return InsertOutcome(
                        committed = false,
                        fallbackUsed = false,
                        failure = CommitFailure.STALE_SESSION,
                        stale = true
                    )
                }
            }
            if (gateway.sdkInt() < MIN_COMMIT_SDK) {
                return fallbackOnce(payload, CommitFailure.UNSUPPORTED_PLATFORM)
            }
            val mimes = session?.acceptedMimes ?: gateway.editorContentMimes()
            if (!editorSupportsImage(mimes)) {
                return fallbackOnce(payload, CommitFailure.UNSUPPORTED_EDITOR)
            }
            val connection = session?.connection ?: gateway.inputConnection()
                ?: return fallbackOnce(payload, CommitFailure.NO_CONNECTION)

            if (session != null) {
                val current = gateway.currentSession()
                if (current != null && current.sessionId != session.sessionId) {
                    return InsertOutcome(
                        committed = false,
                        fallbackUsed = false,
                        failure = CommitFailure.STALE_SESSION,
                        stale = true
                    )
                }
            }

            val accepted = try {
                gateway.commitImage(connection, payload.uri, payload.mimeType)
            } catch (security: SecurityException) {
                return fallbackOnce(payload, CommitFailure.PERMISSION_DENIED)
            } catch (illegal: IllegalArgumentException) {
                return fallbackOnce(payload, CommitFailure.ILLEGAL_ARGUMENT)
            } catch (_: Exception) {
                return fallbackOnce(payload, CommitFailure.COMMIT_FAILED)
            }
            if (!accepted) {
                return fallbackOnce(payload, CommitFailure.COMMIT_FAILED)
            }
            return InsertOutcome(committed = true, fallbackUsed = false, failure = null)
        } finally {
            inFlightKeys.remove(inFlightKey)
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
