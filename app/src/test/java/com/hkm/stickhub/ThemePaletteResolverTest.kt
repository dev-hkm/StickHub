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

    @Test
    fun testAllCustomSchemesHaveOpaqueSurfaceContainers() {
        // Regression: animateColorScheme rebuilt schemes through the legacy
        // ColorScheme constructor, leaving surfaceContainer* = Unspecified
        // (transparent) — every ModalBottomSheet / AlertDialog turned invisible.
        // All schemes must expose fully-opaque container roles.
        val schemes = listOf(
            HerbariumLightColorScheme,
            HerbariumDarkColorScheme,
            com.hkm.stickhub.ui.theme.SketchbookLightColorScheme,
            com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme,
            com.hkm.stickhub.ui.theme.NeubrutalismLightColorScheme,
            com.hkm.stickhub.ui.theme.NeubrutalismDarkColorScheme,
            com.hkm.stickhub.ui.theme.OldMoneyLightColorScheme,
            com.hkm.stickhub.ui.theme.OldMoneyDarkColorScheme,
            com.hkm.stickhub.ui.theme.PressroomLightColorScheme,
            com.hkm.stickhub.ui.theme.PressroomDarkColorScheme,
            com.hkm.stickhub.ui.theme.AtelierLightColorScheme,
            com.hkm.stickhub.ui.theme.AtelierDarkColorScheme,
            com.hkm.stickhub.ui.theme.StarbaseLightColorScheme,
            com.hkm.stickhub.ui.theme.StarbaseDarkColorScheme,
            com.hkm.stickhub.ui.theme.CottageLightColorScheme,
            com.hkm.stickhub.ui.theme.CottageDarkColorScheme,
            com.hkm.stickhub.ui.theme.AuroraLightColorScheme,
            com.hkm.stickhub.ui.theme.AuroraDarkColorScheme,
            com.hkm.stickhub.ui.theme.SynthwaveLightColorScheme,
            com.hkm.stickhub.ui.theme.SynthwaveDarkColorScheme,
            com.hkm.stickhub.ui.theme.GatsbyLightColorScheme,
            com.hkm.stickhub.ui.theme.GatsbyDarkColorScheme,
            com.hkm.stickhub.ui.theme.UkiyoLightColorScheme,
            com.hkm.stickhub.ui.theme.UkiyoDarkColorScheme,
            com.hkm.stickhub.ui.theme.PixelLightColorScheme,
            com.hkm.stickhub.ui.theme.PixelDarkColorScheme,
            com.hkm.stickhub.ui.theme.KawaiiLightColorScheme,
            com.hkm.stickhub.ui.theme.KawaiiDarkColorScheme,
            com.hkm.stickhub.ui.theme.SolarpunkLightColorScheme,
            com.hkm.stickhub.ui.theme.SolarpunkDarkColorScheme,
            com.hkm.stickhub.ui.theme.NoirLightColorScheme,
            com.hkm.stickhub.ui.theme.NoirDarkColorScheme,
            com.hkm.stickhub.ui.theme.GlassLightColorScheme,
            com.hkm.stickhub.ui.theme.GlassDarkColorScheme,
            com.hkm.stickhub.ui.theme.NouveauLightColorScheme,
            com.hkm.stickhub.ui.theme.NouveauDarkColorScheme
        )
        for (scheme in schemes) {
            val containers = listOf(
                scheme.surfaceBright,
                scheme.surfaceDim,
                scheme.surfaceContainerLowest,
                scheme.surfaceContainerLow,
                scheme.surfaceContainer,
                scheme.surfaceContainerHigh,
                scheme.surfaceContainerHighest
            )
            for (color in containers) {
                assertNotEquals(
                    androidx.compose.ui.graphics.Color.Unspecified,
                    color
                )
                assertEquals(
                    "container role must be fully opaque, was " + color.toArgb().toUInt().toString(16),
                    0xFF,
                    (color.toArgb() ushr 24) and 0xFF
                )
            }
        }
    }

    @Test
    fun testContainerRolesMatchThemeTones() {
        // Dialogs/sheets must feel native to each visual theme, not M3 baseline grey.
        assertEquals(BotanicalColors.LightWarmPaperSurface, HerbariumLightColorScheme.surfaceContainerLow)
        assertEquals(BotanicalColors.LightPaperSurfaceVariant, HerbariumLightColorScheme.surfaceContainerHigh)
        assertEquals(BotanicalColors.DarkPaperSurface, HerbariumDarkColorScheme.surfaceContainerLow)
        assertEquals(BotanicalColors.DarkPaperSurfaceVariant, HerbariumDarkColorScheme.surfaceContainerHigh)
        assertEquals(
            com.hkm.stickhub.ui.theme.SketchbookColors.LightPaperSurface,
            com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.surfaceContainerLow
        )
        assertEquals(
            com.hkm.stickhub.ui.theme.SketchbookColors.LightPaperSurfaceVariant,
            com.hkm.stickhub.ui.theme.SketchbookLightColorScheme.surfaceContainerHigh
        )
        assertEquals(
            com.hkm.stickhub.ui.theme.SketchbookColors.DarkPaperSurface,
            com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.surfaceContainerLow
        )
        assertEquals(
            com.hkm.stickhub.ui.theme.SketchbookColors.DarkPaperSurfaceVariant,
            com.hkm.stickhub.ui.theme.SketchbookDarkColorScheme.surfaceContainerHigh
        )
    }

    @Test
    fun testNeubrutalismColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.NeubrutalismLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightPaperBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightCardSurface, light.surface)
        // Ink primary + candy yellow container: borders read as black, fills pop.
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightInkPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightCandyYellowContainer, light.primaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightSignalRedSecondary, light.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightSignalBlueTertiary, light.tertiary)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightInkText, light.onSurface)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.LightInkOutline, light.outline)

        val dark = com.hkm.stickhub.ui.theme.NeubrutalismDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCoalBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCardSurface, dark.surface)
        // Yellow primary stays legible on coal; borders flip to cream.
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCandyYellowPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCreamOutline, dark.outline)
        assertEquals(com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testNeubrutalismNeverUsesPureBlack() {        // App-wide rule: off-black ink, never #000000.
        val allTones = listOf(
            com.hkm.stickhub.ui.theme.NeubrutalismColors.LightInkPrimary,
            com.hkm.stickhub.ui.theme.NeubrutalismColors.LightInkText,
            com.hkm.stickhub.ui.theme.NeubrutalismColors.LightInkOutline,
            com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCoalBackground,
            com.hkm.stickhub.ui.theme.NeubrutalismColors.DarkCardSurface
        )
        for (tone in allTones) {
            assertNotEquals(0xFF000000.toInt(), tone.toArgb())
        }
    }

    @Test
    fun testOldMoneyColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.OldMoneyLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightIvoryBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightVellumSurface, light.surface)
        // Deep green ink primary, brass container: heritage editorial.
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightDeepGreenPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightBrassPrimaryContainer, light.primaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightOnBrassContainer, light.onPrimaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightGreenInkText, light.onSurface)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.LightBrassOutline, light.outline)

        val dark = com.hkm.stickhub.ui.theme.OldMoneyDarkColorScheme
        // Spec background #114A34 for dark mode.
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.DarkGreenBackground, dark.background)
        assertEquals(0xFF114A34.toInt(), dark.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.DarkGreenSurface, dark.surface)
        // Brass primary stays legible on deep green.
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.DarkBrassPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.DarkOnPrimary, dark.onPrimary)
        assertEquals(com.hkm.stickhub.ui.theme.OldMoneyColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testPressroomColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.PressroomLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.LightNewsprintBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.LightPaperSurface, light.surface)
        // Cocoa ink primary, peach container: warm editorial.
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.LightCocoaPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.LightPeachPrimaryContainer, light.primaryContainer)
        assertEquals(0xFFFFE3C3.toInt(), light.primaryContainer.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.LightOnPeachContainer, light.onPrimaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.LightCocoaText, light.onSurface)

        val dark = com.hkm.stickhub.ui.theme.PressroomDarkColorScheme
        // Spec background #5B3A30 for dark mode.
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.DarkCocoaBackground, dark.background)
        assertEquals(0xFF5B3A30.toInt(), dark.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.DarkCocoaSurface, dark.surface)
        // Peach primary stays legible on cocoa.
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.DarkPeachPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.DarkOnPrimary, dark.onPrimary)
        assertEquals(com.hkm.stickhub.ui.theme.PressroomColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testAtelierColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.AtelierLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.LightGalleryBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.LightGallerySurface, light.surface)
        // Fired terracotta primary, pale terracotta container: gallery minimalism.
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.LightFiredTerracottaPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.LightPaleTerracottaContainer, light.primaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.LightOnTerracottaContainer, light.onPrimaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.LightEspressoText, light.onSurface)

        val dark = com.hkm.stickhub.ui.theme.AtelierDarkColorScheme
        // Spec background #E67D54 for dark mode.
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.DarkTerracottaBackground, dark.background)
        assertEquals(0xFFE67D54.toInt(), dark.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.DarkEmberSurface, dark.surface)
        // Espresso ink primary stays legible on terracotta.
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.DarkEspressoPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.DarkOnPrimary, dark.onPrimary)
        assertEquals(com.hkm.stickhub.ui.theme.AtelierColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testStarbaseColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.StarbaseLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.LightFlightBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.LightFlightSurface, light.surface)
        // Oxblood ink primary, peach container: mission control day.
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.LightOxbloodPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.LightPeachPrimaryContainer, light.primaryContainer)
        assertEquals(0xFFFFE3C3.toInt(), light.primaryContainer.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.LightOnPeachContainer, light.onPrimaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.LightCockpitText, light.onSurface)

        val dark = com.hkm.stickhub.ui.theme.StarbaseDarkColorScheme
        // Spec background #73001C for dark mode.
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.DarkOxbloodBackground, dark.background)
        assertEquals(0xFF73001C.toInt(), dark.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.DarkOxbloodSurface, dark.surface)
        // Peach primary stays legible on oxblood.
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.DarkPeachPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.DarkOnPrimary, dark.onPrimary)
        assertEquals(com.hkm.stickhub.ui.theme.StarbaseColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testCottageColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.CottageLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.LightAntiqueBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.LightCottageSurface, light.surface)
        // Rosewood ink primary, spec faded-rose container.
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.LightRosewoodPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.LightFadedRoseContainer, light.primaryContainer)
        assertEquals(0xFFE8C4C4.toInt(), light.primaryContainer.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.LightOnRoseContainer, light.onPrimaryContainer)
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.LightUmberText, light.onSurface)

        val dark = com.hkm.stickhub.ui.theme.CottageDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.DarkPlumBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.DarkPlumSurface, dark.surface)
        // Faded rose primary glows on plum.
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.DarkFadedRosePrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.DarkOnPrimary, dark.onPrimary)
        assertEquals(com.hkm.stickhub.ui.theme.CottageColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testAuroraColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.AuroraLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.LightDawnBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.LightAuroraBluePrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.LightMagentaSecondary, light.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.LightLagoonTertiary, light.tertiary)
        val dark = com.hkm.stickhub.ui.theme.AuroraDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.DarkNightBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.DarkSkyPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.DarkMagentaSecondary, dark.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.AuroraColors.DarkCyanTertiary, dark.tertiary)
    }

    @Test
    fun testSynthwaveColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.SynthwaveLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.LightLilacBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.LightUltravioletPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.LightHotPinkSecondary, light.secondary)
        val dark = com.hkm.stickhub.ui.theme.SynthwaveDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.DarkVoidBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.DarkNeonBluePrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.DarkHotPinkSecondary, dark.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.SynthwaveColors.DarkCyanTertiary, dark.tertiary)
    }

    @Test
    fun testGatsbyColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.GatsbyLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.GatsbyColors.LightChampagneBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.GatsbyColors.LightTuxedoPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.GatsbyColors.LightGoldPrimaryContainer, light.primaryContainer)
        assertEquals(0xFFD4AF37.toInt(), light.primaryContainer.toArgb())
        val dark = com.hkm.stickhub.ui.theme.GatsbyDarkColorScheme
        assertEquals(0xFF0B0B0E.toInt(), dark.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.GatsbyColors.DarkGoldPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.GatsbyColors.DarkCreamText, dark.onSurface)
    }

    @Test
    fun testUkiyoColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.UkiyoLightColorScheme
        assertEquals(0xFFF0E3CE.toInt(), light.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.UkiyoColors.LightSumiPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.UkiyoColors.LightVermilionSecondary, light.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.UkiyoColors.LightPrussianTertiary, light.tertiary)
        val dark = com.hkm.stickhub.ui.theme.UkiyoDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.UkiyoColors.DarkSeaBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.UkiyoColors.DarkMoonPrimary, dark.primary)
        assertEquals(com.hkm.stickhub.ui.theme.UkiyoColors.DarkVermilionSecondary, dark.secondary)
    }

    @Test
    fun testPixelColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.PixelLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.PixelColors.LightAmberPaperBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.PixelColors.LightPhosphorDeepPrimary, light.primary)
        val dark = com.hkm.stickhub.ui.theme.PixelDarkColorScheme
        assertEquals(0xFF0A0A0A.toInt(), dark.background.toArgb())
        assertEquals(0xFF2CFF56.toInt(), dark.primary.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.PixelColors.DarkAmberSecondary, dark.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.PixelColors.DarkCoinTertiary, dark.tertiary)
    }

    @Test
    fun testKawaiiColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.KawaiiLightColorScheme
        assertEquals(0xFFFFD1DC.toInt(), light.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.KawaiiColors.LightGrapePrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.KawaiiColors.LightSkySecondary, light.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.KawaiiColors.LightMintTertiary, light.tertiary)
        val dark = com.hkm.stickhub.ui.theme.KawaiiDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.KawaiiColors.DarkGrapeBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.KawaiiColors.DarkCandyPrimary, dark.primary)
    }

    @Test
    fun testSolarpunkColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.SolarpunkLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.SolarpunkColors.LightMorningBackground, light.background)
        assertEquals(0xFF2D6A4F.toInt(), light.primary.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.SolarpunkColors.LightHoneySecondaryContainer, light.secondaryContainer)
        assertEquals(0xFFFFB703.toInt(), com.hkm.stickhub.ui.theme.SolarpunkColors.DarkSolarSecondary.toArgb())
        val dark = com.hkm.stickhub.ui.theme.SolarpunkDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.SolarpunkColors.DarkForestBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.SolarpunkColors.DarkSproutPrimary, dark.primary)
        assertEquals(0xFFFFB703.toInt(), dark.secondary.toArgb())
    }

    @Test
    fun testNoirColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.NoirLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.NoirColors.LightDossierBackground, light.background)
        assertEquals(com.hkm.stickhub.ui.theme.NoirColors.LightInkPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.NoirColors.LightStreetlampSecondary, light.secondary)
        val dark = com.hkm.stickhub.ui.theme.NoirDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.NoirColors.DarkAlleyBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.NoirColors.DarkFogPrimary, dark.primary)
        assertEquals(0xFFE8C547.toInt(), dark.secondary.toArgb())
    }

    @Test
    fun testGlassColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.GlassLightColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.GlassColors.LightSkylightBackground, light.background)
        assertEquals(0xFF0066FF.toInt(), light.primary.toArgb())
        val dark = com.hkm.stickhub.ui.theme.GlassDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.GlassColors.DarkAbyssBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.GlassColors.DarkBeamPrimary, dark.primary)
    }

    @Test
    fun testNouveauColorSchemeMatchesArtDirection() {
        val light = com.hkm.stickhub.ui.theme.NouveauLightColorScheme
        assertEquals(0xFFFFFFF0.toInt(), light.background.toArgb())
        assertEquals(com.hkm.stickhub.ui.theme.NouveauColors.LightDeepTealPrimary, light.primary)
        assertEquals(com.hkm.stickhub.ui.theme.NouveauColors.LightOldGoldSecondary, light.secondary)
        assertEquals(com.hkm.stickhub.ui.theme.NouveauColors.LightBurgundyTertiary, light.tertiary)
        val dark = com.hkm.stickhub.ui.theme.NouveauDarkColorScheme
        assertEquals(com.hkm.stickhub.ui.theme.NouveauColors.DarkLagoonBackground, dark.background)
        assertEquals(com.hkm.stickhub.ui.theme.NouveauColors.DarkSeafoamPrimary, dark.primary)
        assertEquals(0xFFCFB53B.toInt(), dark.secondary.toArgb())
    }
}
