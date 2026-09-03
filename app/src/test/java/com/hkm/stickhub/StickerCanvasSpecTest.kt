package com.hkm.stickhub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StickerCanvasSpecTest {

    @Test
    fun portraitCutout_isCenteredOnAConsistentSquareStickerCanvas() {
        val specClass = runCatching {
            Class.forName("com.hkm.stickhub.data.cutout.StickerCanvasSpec")
        }.getOrNull()
        assertNotNull("A cutout needs a normalized sticker-canvas plan", specClass)

        val instance = specClass!!.getField("INSTANCE").get(null)
        val plan = specClass.getMethod("plan", Int::class.java, Int::class.java)
            .invoke(instance, 100, 400)

        assertEquals(1024, plan.javaClass.getMethod("getCanvasSize").invoke(plan))
        assertEquals(200, plan.javaClass.getMethod("getContentWidth").invoke(plan))
        assertEquals(800, plan.javaClass.getMethod("getContentHeight").invoke(plan))
        assertEquals(412, plan.javaClass.getMethod("getLeft").invoke(plan))
        assertEquals(112, plan.javaClass.getMethod("getTop").invoke(plan))
    }
}
