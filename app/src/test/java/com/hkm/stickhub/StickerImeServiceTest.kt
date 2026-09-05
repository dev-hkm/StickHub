package com.hkm.stickhub

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import com.hkm.stickhub.ime.StickerInputMethodService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 11-E (service half): the IME service creates and tears down cleanly
 * with no Activity coupling. Asserts lifecycle ordering, ViewTree owners,
 * ComposeView measurement, and input session snapshot tracking.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StickerImeServiceTest {

    @Test
    fun serviceLifecycleDoesNotCrash() {
        val service = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
            .get()
        assertNotNull(service)
        assertEquals(Lifecycle.State.CREATED, service.lifecycle.currentState)
    }

    @Test
    fun serviceDestroyCancelsCleanly() {
        val controller = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
        val service = controller.get()
        assertNotNull(service)
        controller.destroy()
        assertEquals(Lifecycle.State.DESTROYED, service.lifecycle.currentState)
    }

    @Test
    fun viewTreeOwnersAttachedOnCreateInputView() {
        val controller = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
        val service = controller.get()

        val inputView = service.onCreateInputView()
        assertNotNull(inputView)
        assertTrue(inputView is ComposeView)

        assertSame(service, inputView.findViewTreeLifecycleOwner())
        assertSame(service, inputView.findViewTreeViewModelStoreOwner())
        assertSame(service, inputView.findViewTreeSavedStateRegistryOwner())
    }

    @Test
    fun inputViewMeasuresAndLayoutsWithoutCrash() {
        val controller = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
        val service = controller.get()

        val inputView = service.onCreateInputView()
        inputView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY)
        )
        inputView.layout(0, 0, 1080, 800)

        assertEquals(1080, inputView.measuredWidth)
        assertEquals(800, inputView.measuredHeight)
    }

    @Test
    fun duplicateLifecycleCallbacksHandledGracefully() {
        val controller = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
        val service = controller.get()

        val editorInfo = EditorInfo().apply {
            packageName = "com.test.editor"
            fieldId = 100
        }

        service.onStartInput(editorInfo, false)
        service.onStartInput(editorInfo, true)
        assertEquals(Lifecycle.State.STARTED, service.lifecycle.currentState)

        service.onStartInputView(editorInfo, false)
        service.onStartInputView(editorInfo, true)
        assertEquals(Lifecycle.State.RESUMED, service.lifecycle.currentState)

        service.onFinishInputView(false)
        service.onFinishInputView(true)
        assertEquals(Lifecycle.State.STARTED, service.lifecycle.currentState)

        service.onFinishInput()
        service.onFinishInput()
        assertEquals(Lifecycle.State.CREATED, service.lifecycle.currentState)

        service.onUnbindInput()
        assertEquals(Lifecycle.State.CREATED, service.lifecycle.currentState)

        controller.destroy()
        assertEquals(Lifecycle.State.DESTROYED, service.lifecycle.currentState)
    }

    @Test
    fun recreationFlowMaintainsLifecycle() {
        val controller = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
        val service = controller.get()

        val info = EditorInfo().apply {
            packageName = "com.example.chat"
            contentMimeTypes = arrayOf("image/png")
        }

        service.onStartInput(info, false)
        val firstView = service.onCreateInputView()
        assertNotNull(firstView)

        service.onStartInputView(info, false)
        assertTrue(service.editorSupportsRichInsert())

        // Recreate input view (e.g. config change)
        val secondView = service.onCreateInputView()
        assertNotNull(secondView)
        assertSame(service, secondView.findViewTreeLifecycleOwner())

        service.onFinishInputView(true)
        service.onFinishInput()
        controller.destroy()
        assertEquals(Lifecycle.State.DESTROYED, service.lifecycle.currentState)
    }
}
