package com.hkm.stickhub

import android.net.Uri
import android.view.inputmethod.InputConnection
import com.hkm.stickhub.ime.StickerImeInsertController
import com.hkm.stickhub.util.StickerExportService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Phase 11-E: insert orchestration — exactly one commit attempt per tap,
 * exactly one clipboard fallback, never both, never duplicates.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StickerImeControllerTest {

    private val payload = StickerExportService.ExportPayload(
        file = File("/tmp/share_ime_test.png"),
        uri = Uri.parse("content://com.hkm.stickhub.stickerprovider/clipboard/share_ime_test.png"),
        mimeType = "image/png",
        sourceIdentity = "share_ime_test.png:10",
        purpose = StickerExportService.ExportPurpose.IME,
        fromOriginal = false
    )

    private class FakeGateway(
        var sdk: Int = 34,
        var mimes: Array<String>? = arrayOf("image/png"),
        var connection: InputConnection? = FakeConnection(),
        var commitResult: Boolean = true,
        var commitThrows: Throwable? = null,
        var fallbackResult: Boolean = true
    ) : StickerImeInsertController.Gateway {
        val commits = AtomicInteger(0)
        val fallbacks = AtomicInteger(0)
        var gate: CountDownLatch? = null

        override fun sdkInt(): Int = sdk
        override fun editorContentMimes(): Array<String>? = mimes
        override fun inputConnection(): InputConnection? = connection
        override fun commitImage(connection: InputConnection, uri: Uri, mimeType: String): Boolean {
            commits.incrementAndGet()
            gate?.await(5, TimeUnit.SECONDS)
            commitThrows?.let { throw it }
            return commitResult
        }

        override fun fallbackCopy(uri: Uri, mimeType: String): Boolean {
            fallbacks.incrementAndGet()
            return fallbackResult
        }
    }

    private class FakeConnection : InputConnection {
        override fun getTextBeforeCursor(n: Int, flags: Int) = null
        override fun getTextAfterCursor(n: Int, flags: Int) = null
        override fun getSelectedText(flags: Int) = null
        override fun getCursorCapsMode(reqModes: Int) = 0
        override fun getExtractedText(request: android.view.inputmethod.ExtractedTextRequest?, flags: Int) = null
        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int) = false
        override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int) = false
        override fun setComposingText(text: CharSequence?, newCursorPosition: Int) = false
        override fun setComposingRegion(start: Int, end: Int) = false
        override fun finishComposingText() = false
        override fun commitText(text: CharSequence?, newCursorPosition: Int) = false
        override fun commitCompletion(text: android.view.inputmethod.CompletionInfo?) = false
        override fun commitCorrection(correctionInfo: android.view.inputmethod.CorrectionInfo?) = false
        override fun setSelection(start: Int, end: Int) = false
        override fun performEditorAction(editorAction: Int) = false
        override fun performContextMenuAction(id: Int) = false
        override fun beginBatchEdit() = false
        override fun endBatchEdit() = false
        override fun sendKeyEvent(event: android.view.KeyEvent?) = false
        override fun clearMetaKeyStates(states: Int) = false
        override fun reportFullscreenMode(enabled: Boolean) = false
        override fun performPrivateCommand(action: String?, data: android.os.Bundle?) = false
        override fun requestCursorUpdates(cursorUpdateMode: Int) = false
        override fun getHandler(): android.os.Handler? = null
        override fun closeConnection() {}
        override fun commitContent(inputContentInfo: android.view.inputmethod.InputContentInfo, flags: Int, opts: android.os.Bundle?) = false
    }

    @Test
    fun pngEditorCommitsExactlyOnceWithoutFallback() {
        val gateway = FakeGateway()
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertTrue(outcome.committed)
        assertFalse(outcome.fallbackUsed)
        assertNull(outcome.failure)
        assertEquals(1, gateway.commits.get())
        assertEquals(0, gateway.fallbacks.get())
    }

    @Test
    fun wildcardEditorCommits() {
        val gateway = FakeGateway(mimes = arrayOf("image/*"))
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertTrue(outcome.committed)
        assertEquals(1, gateway.commits.get())
    }

    @Test
    fun editorWithoutRichContentFallsBackOnce() {
        val gateway = FakeGateway(mimes = arrayOf("text/plain"))
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertFalse(outcome.committed)
        assertTrue(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.UNSUPPORTED_EDITOR, outcome.failure)
        assertEquals(0, gateway.commits.get())
        assertEquals(1, gateway.fallbacks.get())
    }

    @Test
    fun unknownEditorCapabilitiesFallBack() {
        val gateway = FakeGateway(mimes = null)
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertTrue(outcome.fallbackUsed)
        assertEquals(0, gateway.commits.get())
    }

    @Test
    fun rejectedCommitFallsBackOnce() {
        val gateway = FakeGateway(commitResult = false)
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertFalse(outcome.committed)
        assertTrue(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.COMMIT_FAILED, outcome.failure)
        assertEquals(1, gateway.commits.get())
        assertEquals(1, gateway.fallbacks.get())
    }

    @Test
    fun nullConnectionFallsBackWithoutCommit() {
        val gateway = FakeGateway(connection = null)
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertTrue(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.NO_CONNECTION, outcome.failure)
        assertEquals(0, gateway.commits.get())
    }

    @Test
    fun permissionFailureSurfacesExplicitly() {
        val gateway = FakeGateway(commitThrows = SecurityException("grant denied"))
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertFalse(outcome.committed)
        assertTrue(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.PERMISSION_DENIED, outcome.failure)
    }

    @Test
    fun failedFallbackReportsClearly() {
        val gateway = FakeGateway(commitResult = false, fallbackResult = false)
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertFalse(outcome.committed)
        assertFalse(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.FALLBACK_FAILED, outcome.failure)
    }

    @Test
    fun pre25PlatformSkipsCommitForClipboard() {
        val gateway = FakeGateway(sdk = 24)
        val outcome = StickerImeInsertController(gateway).insertSticker(payload)
        assertFalse(outcome.committed)
        assertTrue(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.UNSUPPORTED_PLATFORM, outcome.failure)
        assertEquals(0, gateway.commits.get())
    }

    @Test
    fun nullPayloadNeverTouchesGateway() {
        val gateway = FakeGateway()
        val outcome = StickerImeInsertController(gateway).insertSticker(null)
        assertFalse(outcome.committed)
        assertFalse(outcome.fallbackUsed)
        assertEquals(StickerImeInsertController.CommitFailure.NO_PAYLOAD, outcome.failure)
        assertEquals(0, gateway.commits.get())
        assertEquals(0, gateway.fallbacks.get())
    }

    @Test
    fun sequentialTapsCommitIndependently() {
        val gateway = FakeGateway()
        val controller = StickerImeInsertController(gateway)
        assertTrue(controller.insertSticker(payload).committed)
        assertTrue(controller.insertSticker(payload).committed)
        assertEquals(2, gateway.commits.get())
        assertEquals(0, gateway.fallbacks.get())
    }

    @Test
    fun concurrentSecondTapIsIgnored() {
        val gateway = FakeGateway()
        gateway.gate = CountDownLatch(1)
        val controller = StickerImeInsertController(gateway)
        var first: StickerImeInsertController.InsertOutcome? = null
        val worker = Thread { first = controller.insertSticker(payload) }
        worker.start()
        // Wait until the first tap is inside the commit.
        val deadline = System.currentTimeMillis() + 4000
        while (gateway.commits.get() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        val second = controller.insertSticker(payload)
        assertTrue(second.ignoredDueToInflight)
        gateway.gate!!.countDown()
        worker.join(5000)
        assertTrue(first!!.committed)
        assertEquals(1, gateway.commits.get())
        assertEquals(0, gateway.fallbacks.get())
    }

    @Test
    fun editorMimeMatchingIsExact() {
        assertTrue(StickerImeInsertController.editorSupportsImage(arrayOf("image/png")))
        assertTrue(StickerImeInsertController.editorSupportsImage(arrayOf("image/*")))
        assertTrue(StickerImeInsertController.editorSupportsImage(arrayOf("text/plain", "image/png")))
        assertTrue(!StickerImeInsertController.editorSupportsImage(arrayOf("text/plain")))
        assertTrue(!StickerImeInsertController.editorSupportsImage(arrayOf("image/jpeg")))
        assertTrue(!StickerImeInsertController.editorSupportsImage(null))
        assertTrue(!StickerImeInsertController.editorSupportsImage(arrayOf()))
    }
}
