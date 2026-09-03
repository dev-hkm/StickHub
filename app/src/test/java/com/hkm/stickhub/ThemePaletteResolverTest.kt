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
    }
}
