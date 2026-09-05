package com.hkm.stickhub

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IconVisualTest {

    @Test
    fun monochromeLauncherIconLoadsAndRenders() {
        val context = RuntimeEnvironment.getApplication()
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_stickhub_launcher_monochrome)
        assertNotNull("Monochrome icon drawable must be present", drawable)

        val bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(0, 0, 108, 108)
        drawable.draw(canvas)
    }
}
