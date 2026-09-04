package com.hkm.stickhub

import android.graphics.Bitmap
import com.hkm.stickhub.data.cutout.SubjectCutoutProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CutoutModelInputTest {
    @Test
    fun narrowPhotoDoesNotExpandBeyondTheModelPixelBudget() {
        val source = Bitmap.createBitmap(2048, 64, Bitmap.Config.ARGB_8888)
        val processor = SubjectCutoutProcessor(RuntimeEnvironment.getApplication())
        val method = SubjectCutoutProcessor::class.java.getDeclaredMethod("scaleForSubjectModel", Bitmap::class.java)
        method.isAccessible = true
        val result = method.invoke(processor, source) as Bitmap
        try {
            assertTrue("Longest side must remain at most 2048", maxOf(result.width, result.height) <= 2048)
            assertTrue("Model still receives at least 512 pixels per side", minOf(result.width, result.height) >= 512)
            assertTrue(result.width.toLong() * result.height <= 2048L * 2048L)
        } finally {
            if (result !== source) result.recycle()
            source.recycle()
        }
    }

    @Test
    fun ordinaryPhotoKeepsItsDimensions() {
        val source = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888)
        val processor = SubjectCutoutProcessor(RuntimeEnvironment.getApplication())
        val method = SubjectCutoutProcessor::class.java.getDeclaredMethod("scaleForSubjectModel", Bitmap::class.java)
        method.isAccessible = true
        val result = method.invoke(processor, source) as Bitmap
        try {
            assertEquals(1024, result.width)
            assertEquals(768, result.height)
        } finally {
            if (result !== source) result.recycle()
            source.recycle()
        }
    }
}
