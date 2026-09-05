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
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IconVisualTest {

    @Test
    fun monochromeLauncherIconsLoadAndRender() {
        val context = RuntimeEnvironment.getApplication()

        // Verify ic_launcher_monochrome
        val icLauncherMonochrome = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_monochrome)
        assertNotNull("ic_launcher_monochrome must be present", icLauncherMonochrome)

        // Verify ic_stickhub_launcher_monochrome
        val icStickhubLauncherMonochrome = AppCompatResources.getDrawable(context, R.drawable.ic_stickhub_launcher_monochrome)
        assertNotNull("ic_stickhub_launcher_monochrome must be present", icStickhubLauncherMonochrome)

        val outDir = File("build/icon_renders")
        outDir.mkdirs()

        // 1. Simulate Material You Light theme
        val bitmapLight = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val canvasLight = Canvas(bitmapLight)
        val paintLightBg = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#DFE2EB")
            isAntiAlias = true
        }
        canvasLight.drawCircle(256f, 256f, 240f, paintLightBg)
        icLauncherMonochrome!!.setTint(android.graphics.Color.parseColor("#43474E"))
        icLauncherMonochrome.setBounds(0, 0, 512, 512)
        icLauncherMonochrome.draw(canvasLight)

        FileOutputStream(File(outDir, "test_mascot_monochrome_light.png")).use { fos ->
            bitmapLight.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        // 2. Simulate Material You Dark theme
        val bitmapDark = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val canvasDark = Canvas(bitmapDark)
        val paintDarkBg = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1A1C1E")
            isAntiAlias = true
        }
        canvasDark.drawCircle(256f, 256f, 240f, paintDarkBg)
        icLauncherMonochrome.setTint(android.graphics.Color.parseColor("#C4C6D0"))
        icLauncherMonochrome.setBounds(0, 0, 512, 512)
        icLauncherMonochrome.draw(canvasDark)

        FileOutputStream(File(outDir, "test_mascot_monochrome_dark.png")).use { fos ->
            bitmapDark.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
    }
}
