package com.hkm.stickhub.cloud

import android.content.Context
import com.hkm.stickhub.service.OverlayPreferences
import com.hkm.stickhub.ui.library.StickerLibraryPreferences
import com.hkm.stickhub.ui.library.StickerLibraryViewMode
import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.ThemePreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BackupSettingsSnapshotTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("stickhub_overlay_preferences", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("stickhub_theme_preferences", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("stickhub_library_preferences", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun snapshotRestoresThemeLibraryAndOverlaySettings() {
        ThemePreferences.setThemeMode(context, AppThemeMode.DARK)
        StickerLibraryPreferences.setViewMode(context, StickerLibraryViewMode.LIST)
        OverlayPreferences.setBubbleSizeDp(context, 56f)
        OverlayPreferences.setShowCategories(context, false)

        val snapshot = BackupSettingsSnapshot.capture(context)

        ThemePreferences.setThemeMode(context, AppThemeMode.LIGHT)
        StickerLibraryPreferences.setViewMode(context, StickerLibraryViewMode.COMPACT_GRID)
        OverlayPreferences.setBubbleSizeDp(context, 32f)
        OverlayPreferences.setShowCategories(context, true)

        BackupSettingsSnapshot.restore(context, snapshot)

        assertEquals(AppThemeMode.DARK, ThemePreferences.getThemeMode(context))
        assertEquals(StickerLibraryViewMode.LIST, StickerLibraryPreferences.getViewMode(context))
        assertEquals(56f, OverlayPreferences.bubbleSizeDp(context), 0f)
        assertEquals(false, OverlayPreferences.showCategories(context))
    }
}
