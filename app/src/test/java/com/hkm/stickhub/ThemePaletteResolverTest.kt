package com.hkm.stickhub

import androidx.compose.ui.graphics.toArgb
import com.hkm.stickhub.ui.theme.AppVisualTheme
import com.hkm.stickhub.ui.theme.BotanicalColors
import com.hkm.stickhub.ui.theme.HerbariumDarkColorScheme
import com.hkm.stickhub.ui.theme.HerbariumLightColorScheme
import com.hkm.stickhub.ui.theme.ThemePaletteResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemePaletteResolverTest {

    @Test
    fun testHerbariumLightColorSchemeMatchesArtDirection() {
        assertEquals(BotanicalColors.LightParchmentBackground, HerbariumLightColorScheme.background)
        assertEquals(BotanicalColors.LightWarmPaperSurface, HerbariumLightColorScheme.surface)
        assertEquals(BotanicalColors.LightLeafGreenPrimary, HerbariumLightColorScheme.primary)
        assertEquals(BotanicalColors.LightSagePrimaryContainer, HerbariumLightColorScheme.primaryContainer)
        assertEquals(BotanicalColors.LightEarthBrownSecondary, HerbariumLightColorScheme.secondary)
        assertEquals(BotanicalColors.LightMutedTerracottaTertiary, HerbariumLightColorScheme.tertiary)
        assertEquals(BotanicalColors.LightBotanicalInkText, HerbariumLightColorScheme.onSurface)
        assertEquals(BotanicalColors.LightMutedInkText, HerbariumLightColorScheme.onSurfaceVariant)
        assertEquals(BotanicalColors.LightOutline, HerbariumLightColorScheme.outline)
        assertEquals(BotanicalColors.LightMutedRedError, HerbariumLightColorScheme.error)
    }

    @Test
    fun testHerbariumDarkColorSchemeMatchesArtDirection() {
        assertEquals(BotanicalColors.DarkEspressoBackground, HerbariumDarkColorScheme.background)
        assertEquals(BotanicalColors.DarkPaperSurface, HerbariumDarkColorScheme.surface)
        assertEquals(BotanicalColors.DarkSagePrimary, HerbariumDarkColorScheme.primary)
        assertEquals(BotanicalColors.DarkPrimaryContainer, HerbariumDarkColorScheme.primaryContainer)
        assertEquals(BotanicalColors.DarkParchmentBrownSecondary, HerbariumDarkColorScheme.secondary)
        assertEquals(BotanicalColors.DarkMutedRoseTertiary, HerbariumDarkColorScheme.tertiary)
        assertEquals(BotanicalColors.DarkParchmentText, HerbariumDarkColorScheme.onSurface)
        assertEquals(BotanicalColors.DarkMutedText, HerbariumDarkColorScheme.onSurfaceVariant)
        assertEquals(BotanicalColors.DarkOutline, HerbariumDarkColorScheme.outline)
        assertEquals(BotanicalColors.DarkMutedRedError, HerbariumDarkColorScheme.error)
    }

    @Test
    fun testHerbariumNeverProducesPureBlackOrPureWhite() {
        // No pure black (#000000) or pure white (#FFFFFF) for main surfaces/backgrounds
        assertNotEquals(0xFF000000.toInt(), HerbariumDarkColorScheme.background.toArgb())
        assertNotEquals(0xFF000000.toInt(), HerbariumDarkColorScheme.surface.toArgb())
        assertNotEquals(0xFFFFFFFF.toInt(), HerbariumLightColorScheme.background.toArgb())
        assertNotEquals(0xFFFFFFFF.toInt(), HerbariumLightColorScheme.surface.toArgb())
    }

    @Test
    fun testAppVisualThemeMetadata() {
        assertEquals("Default", AppVisualTheme.DEFAULT.title)
        assertEquals("System palette", AppVisualTheme.DEFAULT.subtitle)
        assertEquals("Herbarium", AppVisualTheme.HERBARIUM.title)
        assertEquals("Parchment, botanical ink, and scientific detail", AppVisualTheme.HERBARIUM.subtitle)
        assertEquals("Sketchbook", AppVisualTheme.SKETCHBOOK.title)
        assertEquals("Notebook paper and hand-drawn ink", AppVisualTheme.SKETCHBOOK.subtitle)
    }

    @Test
    fun testSketchbookLightColorSchemeMatchesArtDirection() {
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightNotebookBackground, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.background)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightPaperSurface, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.surface)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightNavyInkPrimary, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.primary)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightPrimaryContainer, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.primaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightBallpointBlueSecondary, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightMutedRedAccent, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.tertiary)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightPencilGraphite, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.onSurface)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightOutline, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.outline)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.LightMutedRedError, com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.error)
    }

    @Test
    fun testSketchbookDarkColorSchemeMatchesArtDirection() {
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkDeepNotebookBackground, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.background)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkPaperSurface, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.surface)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkPrimaryBlue, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.primary)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkPrimaryContainer, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.primaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkSecondaryInkBlue, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkRedAccent, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.tertiary)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkMainPaperText, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.onSurface)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkOutline, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.outline)
        assertEquals(com.hkm.stickhub.ui.theme.SketchbookColors.DarkMutedRedError, com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.error)
    }

    @Test
    fun testSketchbookNeverProducesPureBlackOrNeonColors() {
        assertNotEquals(0xFF000000.toInt(), com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.background.toArgb())
        assertNotEquals(0xFF000000.toInt(), com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.surface.toArgb())
        assertNotEquals(0xFF0000FF.toInt(), com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.primary.toArgb())
        assertNotEquals(0xFFFFFFA5.toInt(), com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.surface.toArgb())
    }
}
