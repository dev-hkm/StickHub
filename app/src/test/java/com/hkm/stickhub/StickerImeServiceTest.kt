package com.hkm.stickhub

import com.hkm.stickhub.ime.StickerInputMethodService
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 11-E (service half): the IME service creates and tears down cleanly
 * with no Activity coupling. Insert orchestration itself is covered by
 * [StickerImeControllerTest] with a fake gateway.
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
    }

    @Test
    fun serviceDestroyCancelsCleanly() {
        val controller = Robolectric.buildService(StickerInputMethodService::class.java)
            .create()
        val service = controller.get()
        assertNotNull(service)
        controller.destroy()
    }
}
