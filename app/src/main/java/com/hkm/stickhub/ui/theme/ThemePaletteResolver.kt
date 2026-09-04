package com.hkm.stickhub.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Curated color tokens for the Herbarium ("Vintage Botanical Scientific Illustration") theme.
 */
object BotanicalColors {
    // Light Palette
    val LightParchmentBackground = Color(0xFFF0E5D3)
    val LightWarmPaperSurface = Color(0xFFFAF4EA)
    val LightPaperSurfaceVariant = Color(0xFFEAE0CE)
    val LightLeafGreenPrimary = Color(0xFF4A6741)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightSagePrimaryContainer = Color(0xFFDDE6D7)
    val LightOnPrimaryContainer = Color(0xFF132B0F)
    val LightEarthBrownSecondary = Color(0xFF5D4037)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightSecondaryContainer = Color(0xFFE7DDD3)
    val LightOnSecondaryContainer = Color(0xFF1D140F)
    val LightMutedTerracottaTertiary = Color(0xFFBF6B63)
    val LightOnTertiary = Color(0xFFFFFFFF)
    val LightTertiaryContainer = Color(0xFFF6DCD9)
    val LightOnTertiaryContainer = Color(0xFF3C100E)
    val LightBotanicalInkText = Color(0xFF1A1512)
    val LightMutedInkText = Color(0xFF62564B)
    val LightOutline = Color(0xFFA89480)
    val LightOutlineVariant = Color(0xFFC8B8A6)
    val LightMutedRedError = Color(0xFF9C413D)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFFFDAD6)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette
    val DarkEspressoBackground = Color(0xFF171511)
    val DarkPaperSurface = Color(0xFF211C17)
    val DarkPaperSurfaceVariant = Color(0xFF2E2720)
    val DarkSagePrimary = Color(0xFFB7C9AA)
    val DarkOnPrimary = Color(0xFF23351E)
    val DarkPrimaryContainer = Color(0xFF354535)
    val DarkOnPrimaryContainer = Color(0xFFCCE1C0)
    val DarkParchmentBrownSecondary = Color(0xFFD5BA9F)
    val DarkOnSecondary = Color(0xFF3B2B1B)
    val DarkSecondaryContainer = Color(0xFF4D3A29)
    val DarkOnSecondaryContainer = Color(0xFFF2D8BD)
    val DarkMutedRoseTertiary = Color(0xFFE0A097)
    val DarkOnTertiary = Color(0xFF431B16)
    val DarkTertiaryContainer = Color(0xFF5A2C26)
    val DarkOnTertiaryContainer = Color(0xFFFFDAD5)
    val DarkParchmentText = Color(0xFFF0E5D3)
    val DarkMutedText = Color(0xFFD2C3B1)
    val DarkOutline = Color(0xFF9B8874)
    val DarkOutlineVariant = Color(0xFF50453A)
    val DarkMutedRedError = Color(0xFFFFB4AB)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)
}

/**
 * Curated color tokens for the Sketchbook ("Hand-Drawn Sketch-Note") theme.
 */
object SketchbookColors {
    // Light Palette
    val LightNotebookBackground = Color(0xFFF4F1EA)
    val LightPaperSurface = Color(0xFFFFFCF5)
    val LightPaperSurfaceVariant = Color(0xFFEBE6DC)
    val LightNavyInkPrimary = Color(0xFF0E2A5C)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightPrimaryContainer = Color(0xFFDCE7F7)
    val LightOnPrimaryContainer = Color(0xFF081B3C)
    val LightBallpointBlueSecondary = Color(0xFF3158A6)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightSecondaryContainer = Color(0xFFD7E2F7)
    val LightOnSecondaryContainer = Color(0xFF0D1D3A)
    val LightPencilGraphite = Color(0xFF333333)
    val LightMutedGraphite = Color(0xFF67635E)
    val LightMutedRedAccent = Color(0xFFB84942)
    val LightOnTertiary = Color(0xFFFFFFFF)
    val LightTertiaryContainer = Color(0xFFF9DCDA)
    val LightOnTertiaryContainer = Color(0xFF3D100D)
    val LightHighlighterYellow = Color(0xFFF3E899)
    val LightOnHighlighterYellow = Color(0xFF2B2600)
    val LightOutline = Color(0xFF9B968D)
    val LightOutlineVariant = Color(0xFFCCC6BC)
    val LightMutedRedError = Color(0xFFB84942)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFFFDAD6)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette
    val DarkDeepNotebookBackground = Color(0xFF121A29)
    val DarkPaperSurface = Color(0xFF1B2433)
    val DarkPaperSurfaceVariant = Color(0xFF243044)
    val DarkPrimaryBlue = Color(0xFFB8CCFF)
    val DarkOnPrimary = Color(0xFF002B73)
    val DarkPrimaryContainer = Color(0xFF2C4679)
    val DarkOnPrimaryContainer = Color(0xFFDCE7F7)
    val DarkSecondaryInkBlue = Color(0xFF9FC0FF)
    val DarkOnSecondary = Color(0xFF002A66)
    val DarkSecondaryContainer = Color(0xFF1C3A6E)
    val DarkOnSecondaryContainer = Color(0xFFD7E3FF)
    val DarkMainPaperText = Color(0xFFF4F1EA)
    val DarkMutedPaperText = Color(0xFFCDC7BD)
    val DarkRedAccent = Color(0xFFFFB4AE)
    val DarkOnTertiary = Color(0xFF561E1A)
    val DarkTertiaryContainer = Color(0xFF73332E)
    val DarkOnTertiaryContainer = Color(0xFFFFDAD6)
    val DarkMutedHighlighter = Color(0xFFD7C96B)
    val DarkOnHighlighter = Color(0xFF353000)
    val DarkOutline = Color(0xFFA29C93)
    val DarkOutlineVariant = Color(0xFF454D5D)
    val DarkMutedRedError = Color(0xFFFFB4AE)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)
}

/**
 * Curated color tokens for the Neubrutalism theme ("Bold borders, hard shadows").
 * Off-black ink instead of pure black per app-wide rule; candy yellow/red/blue fills.
 */
object NeubrutalismColors {
    // Light Palette
    val LightPaperBackground = Color(0xFFFFFDF7)
    val LightCardSurface = Color(0xFFFFFFFF)
    val LightCreamSurfaceVariant = Color(0xFFF6EEDA)
    val LightInkPrimary = Color(0xFF161616)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightCandyYellowContainer = Color(0xFFFFEB3B)
    val LightOnYellowContainer = Color(0xFF161616)
    val LightSignalRedSecondary = Color(0xFFC93A3A)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightBlushSecondaryContainer = Color(0xFFFFDCDC)
    val LightOnSecondaryContainer = Color(0xFF3D0A0A)
    val LightSignalBlueTertiary = Color(0xFF1E88E5)
    val LightOnTertiary = Color(0xFFFFFFFF)
    val LightSkyTertiaryContainer = Color(0xFFD6EAFB)
    val LightOnTertiaryContainer = Color(0xFF0B2A4A)
    val LightInkText = Color(0xFF161616)
    val LightMutedText = Color(0xFF6B6250)
    val LightInkOutline = Color(0xFF161616)
    val LightSoftOutlineVariant = Color(0xFFE0D8C4)
    val LightError = Color(0xFFC62828)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFFFDAD6)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette
    val DarkCoalBackground = Color(0xFF141417)
    val DarkCardSurface = Color(0xFF1C1C21)
    val DarkSlateSurfaceVariant = Color(0xFF2A2A32)
    val DarkCandyYellowPrimary = Color(0xFFFFEB3B)
    val DarkOnPrimary = Color(0xFF161616)
    val DarkOlivePrimaryContainer = Color(0xFF5F5705)
    val DarkOnPrimaryContainer = Color(0xFFFFF9C4)
    val DarkSignalRedSecondary = Color(0xFFFF6B6B)
    val DarkOnSecondary = Color(0xFF3D0A0A)
    val DarkMaroonSecondaryContainer = Color(0xFF5C2222)
    val DarkOnSecondaryContainer = Color(0xFFFFD9D9)
    val DarkSignalBlueTertiary = Color(0xFF64B5F6)
    val DarkOnTertiary = Color(0xFF0B2A4A)
    val DarkNavyTertiaryContainer = Color(0xFF173A5E)
    val DarkOnTertiaryContainer = Color(0xFFD6EAFB)
    val DarkCreamText = Color(0xFFF5F0DC)
    val DarkMutedText = Color(0xFFB9B2A0)
    val DarkCreamOutline = Color(0xFFF2ECDA)
    val DarkSoftOutlineVariant = Color(0xFF4C4C56)
    val DarkError = Color(0xFFFF6B6B)
    val DarkOnError = Color(0xFF3D0A0A)
    val DarkErrorContainer = Color(0xFF5C2222)
    val DarkOnErrorContainer = Color(0xFFFFD9D9)
}

val HerbariumLightColorScheme: ColorScheme = lightColorScheme(
    primary = BotanicalColors.LightLeafGreenPrimary,
    onPrimary = BotanicalColors.LightOnPrimary,
    primaryContainer = BotanicalColors.LightSagePrimaryContainer,
    onPrimaryContainer = BotanicalColors.LightOnPrimaryContainer,
    secondary = BotanicalColors.LightEarthBrownSecondary,
    onSecondary = BotanicalColors.LightOnSecondary,
    secondaryContainer = BotanicalColors.LightSecondaryContainer,
    onSecondaryContainer = BotanicalColors.LightOnSecondaryContainer,
    tertiary = BotanicalColors.LightMutedTerracottaTertiary,
    onTertiary = BotanicalColors.LightOnTertiary,
    tertiaryContainer = BotanicalColors.LightTertiaryContainer,
    onTertiaryContainer = BotanicalColors.LightOnTertiaryContainer,
    background = BotanicalColors.LightParchmentBackground,
    onBackground = BotanicalColors.LightBotanicalInkText,
    surface = BotanicalColors.LightWarmPaperSurface,
    onSurface = BotanicalColors.LightBotanicalInkText,
    surfaceVariant = BotanicalColors.LightPaperSurfaceVariant,
    onSurfaceVariant = BotanicalColors.LightMutedInkText,
    outline = BotanicalColors.LightOutline,
    outlineVariant = BotanicalColors.LightOutlineVariant,
    error = BotanicalColors.LightMutedRedError,
    onError = BotanicalColors.LightOnError,
    errorContainer = BotanicalColors.LightErrorContainer,
    onErrorContainer = BotanicalColors.LightOnErrorContainer,
    // Opaque surface-container roles derived from paper tones so
    // ModalBottomSheet / AlertDialog never render transparent.
    surfaceBright = BotanicalColors.LightWarmPaperSurface,
    surfaceDim = BotanicalColors.LightPaperSurfaceVariant,
    surfaceContainerLowest = BotanicalColors.LightWarmPaperSurface,
    surfaceContainerLow = BotanicalColors.LightWarmPaperSurface,
    surfaceContainer = BotanicalColors.LightPaperSurfaceVariant,
    surfaceContainerHigh = BotanicalColors.LightPaperSurfaceVariant,
    surfaceContainerHighest = BotanicalColors.LightPaperSurfaceVariant
)

val HerbariumDarkColorScheme: ColorScheme = darkColorScheme(
    primary = BotanicalColors.DarkSagePrimary,
    onPrimary = BotanicalColors.DarkOnPrimary,
    primaryContainer = BotanicalColors.DarkPrimaryContainer,
    onPrimaryContainer = BotanicalColors.DarkOnPrimaryContainer,
    secondary = BotanicalColors.DarkParchmentBrownSecondary,
    onSecondary = BotanicalColors.DarkOnSecondary,
    secondaryContainer = BotanicalColors.DarkSecondaryContainer,
    onSecondaryContainer = BotanicalColors.DarkOnSecondaryContainer,
    tertiary = BotanicalColors.DarkMutedRoseTertiary,
    onTertiary = BotanicalColors.DarkOnTertiary,
    tertiaryContainer = BotanicalColors.DarkTertiaryContainer,
    onTertiaryContainer = BotanicalColors.DarkOnTertiaryContainer,
    background = BotanicalColors.DarkEspressoBackground,
    onBackground = BotanicalColors.DarkParchmentText,
    surface = BotanicalColors.DarkPaperSurface,
    onSurface = BotanicalColors.DarkParchmentText,
    surfaceVariant = BotanicalColors.DarkPaperSurfaceVariant,
    onSurfaceVariant = BotanicalColors.DarkMutedText,
    outline = BotanicalColors.DarkOutline,
    outlineVariant = BotanicalColors.DarkOutlineVariant,
    error = BotanicalColors.DarkMutedRedError,
    onError = BotanicalColors.DarkOnError,
    errorContainer = BotanicalColors.DarkErrorContainer,
    onErrorContainer = BotanicalColors.DarkOnErrorContainer,
    surfaceBright = BotanicalColors.DarkPaperSurfaceVariant,
    surfaceDim = BotanicalColors.DarkPaperSurface,
    surfaceContainerLowest = BotanicalColors.DarkPaperSurface,
    surfaceContainerLow = BotanicalColors.DarkPaperSurface,
    surfaceContainer = BotanicalColors.DarkPaperSurfaceVariant,
    surfaceContainerHigh = BotanicalColors.DarkPaperSurfaceVariant,
    surfaceContainerHighest = BotanicalColors.DarkPaperSurfaceVariant
)

val SketchbookLightColorScheme: ColorScheme = lightColorScheme(
    primary = SketchbookColors.LightNavyInkPrimary,
    onPrimary = SketchbookColors.LightOnPrimary,
    primaryContainer = SketchbookColors.LightPrimaryContainer,
    onPrimaryContainer = SketchbookColors.LightOnPrimaryContainer,
    secondary = SketchbookColors.LightBallpointBlueSecondary,
    onSecondary = SketchbookColors.LightOnSecondary,
    secondaryContainer = SketchbookColors.LightSecondaryContainer,
    onSecondaryContainer = SketchbookColors.LightOnSecondaryContainer,
    tertiary = SketchbookColors.LightMutedRedAccent,
    onTertiary = SketchbookColors.LightOnTertiary,
    tertiaryContainer = SketchbookColors.LightTertiaryContainer,
    onTertiaryContainer = SketchbookColors.LightOnTertiaryContainer,
    background = SketchbookColors.LightNotebookBackground,
    onBackground = SketchbookColors.LightPencilGraphite,
    surface = SketchbookColors.LightPaperSurface,
    onSurface = SketchbookColors.LightPencilGraphite,
    surfaceVariant = SketchbookColors.LightPaperSurfaceVariant,
    onSurfaceVariant = SketchbookColors.LightMutedGraphite,
    outline = SketchbookColors.LightOutline,
    outlineVariant = SketchbookColors.LightOutlineVariant,
    error = SketchbookColors.LightMutedRedError,
    onError = SketchbookColors.LightOnError,
    errorContainer = SketchbookColors.LightErrorContainer,
    onErrorContainer = SketchbookColors.LightOnErrorContainer,
    surfaceBright = SketchbookColors.LightPaperSurface,
    surfaceDim = SketchbookColors.LightPaperSurfaceVariant,
    surfaceContainerLowest = SketchbookColors.LightPaperSurface,
    surfaceContainerLow = SketchbookColors.LightPaperSurface,
    surfaceContainer = SketchbookColors.LightPaperSurfaceVariant,
    surfaceContainerHigh = SketchbookColors.LightPaperSurfaceVariant,
    surfaceContainerHighest = SketchbookColors.LightPaperSurfaceVariant
)

val SketchbookDarkColorScheme: ColorScheme = darkColorScheme(
    primary = SketchbookColors.DarkPrimaryBlue,
    onPrimary = SketchbookColors.DarkOnPrimary,
    primaryContainer = SketchbookColors.DarkPrimaryContainer,
    onPrimaryContainer = SketchbookColors.DarkOnPrimaryContainer,
    secondary = SketchbookColors.DarkSecondaryInkBlue,
    onSecondary = SketchbookColors.DarkOnSecondary,
    secondaryContainer = SketchbookColors.DarkSecondaryContainer,
    onSecondaryContainer = SketchbookColors.DarkOnSecondaryContainer,
    tertiary = SketchbookColors.DarkRedAccent,
    onTertiary = SketchbookColors.DarkOnTertiary,
    tertiaryContainer = SketchbookColors.DarkTertiaryContainer,
    onTertiaryContainer = SketchbookColors.DarkOnTertiaryContainer,
    background = SketchbookColors.DarkDeepNotebookBackground,
    onBackground = SketchbookColors.DarkMainPaperText,
    surface = SketchbookColors.DarkPaperSurface,
    onSurface = SketchbookColors.DarkMainPaperText,
    surfaceVariant = SketchbookColors.DarkPaperSurfaceVariant,
    onSurfaceVariant = SketchbookColors.DarkMutedPaperText,
    outline = SketchbookColors.DarkOutline,
    outlineVariant = SketchbookColors.DarkOutlineVariant,
    error = SketchbookColors.DarkMutedRedError,
    onError = SketchbookColors.DarkOnError,
    errorContainer = SketchbookColors.DarkErrorContainer,
    onErrorContainer = SketchbookColors.DarkOnErrorContainer,
    surfaceBright = SketchbookColors.DarkPaperSurfaceVariant,
    surfaceDim = SketchbookColors.DarkPaperSurface,
    surfaceContainerLowest = SketchbookColors.DarkPaperSurface,
    surfaceContainerLow = SketchbookColors.DarkPaperSurface,
    surfaceContainer = SketchbookColors.DarkPaperSurfaceVariant,
    surfaceContainerHigh = SketchbookColors.DarkPaperSurfaceVariant,
    surfaceContainerHighest = SketchbookColors.DarkPaperSurfaceVariant
)

/**
 * Curated color tokens for the Old Money theme ("Fraunces Typography" — heritage
 * serif, brass gold #C4B773, deep green #114A34).
 */
object OldMoneyColors {
    // Light Palette (daytime heritage: ivory paper, green ink, brass accents)
    val LightIvoryBackground = Color(0xFFF7F3E8)
    val LightVellumSurface = Color(0xFFFFFDF6)
    val LightChampagneSurfaceVariant = Color(0xFFECE4CF)
    val LightDeepGreenPrimary = Color(0xFF114A34)
    val LightOnPrimary = Color(0xFFF7F3E8)
    val LightBrassPrimaryContainer = Color(0xFFC4B773)
    val LightOnBrassContainer = Color(0xFF14261C)
    val LightAntiqueBrassSecondary = Color(0xFF8A6D1F)
    val LightOnSecondary = Color(0xFFFFFDF6)
    val LightSandSecondaryContainer = Color(0xFFEFE5C8)
    val LightOnSecondaryContainer = Color(0xFF3A2F0E)
    val LightEmeraldTertiary = Color(0xFF2E6B4F)
    val LightOnTertiary = Color(0xFFF7F3E8)
    val LightMistTertiaryContainer = Color(0xFFD8E5DC)
    val LightOnTertiaryContainer = Color(0xFF0F2E21)
    val LightGreenInkText = Color(0xFF1C2420)
    val LightMutedText = Color(0xFF5D665E)
    val LightBrassOutline = Color(0xFF8A7B4A)
    val LightSoftOutlineVariant = Color(0xFFD8CFB4)
    val LightError = Color(0xFFA63A32)
    val LightOnError = Color(0xFFFFFDF6)
    val LightErrorContainer = Color(0xFFF5D5D1)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette (evening heritage: deep green room, brass lamplight)
    val DarkGreenBackground = Color(0xFF114A34)
    val DarkGreenSurface = Color(0xFF16382A)
    val DarkGreenSurfaceVariant = Color(0xFF1F4A37)
    val DarkBrassPrimary = Color(0xFFC4B773)
    val DarkOnPrimary = Color(0xFF14261C)
    val DarkBronzePrimaryContainer = Color(0xFF54491F)
    val DarkOnPrimaryContainer = Color(0xFFF1E6BE)
    val DarkChampagneSecondary = Color(0xFFD8C078)
    val DarkOnSecondary = Color(0xFF14261C)
    val DarkUmberSecondaryContainer = Color(0xFF3E3A1E)
    val DarkOnSecondaryContainer = Color(0xFFEFE5C8)
    val DarkSageTertiary = Color(0xFF8FC0A3)
    val DarkOnTertiary = Color(0xFF0F2E21)
    val DarkPineTertiaryContainer = Color(0xFF234A38)
    val DarkOnTertiaryContainer = Color(0xFFD8E5DC)
    val DarkCreamText = Color(0xFFF1EAD6)
    val DarkMutedText = Color(0xFFB9C2B2)
    val DarkBrassOutline = Color(0xFF8A7B4A)
    val DarkSoftOutlineVariant = Color(0xFF2C5240)
    val DarkError = Color(0xFFE89890)
    val DarkOnError = Color(0xFF3A0E0B)
    val DarkErrorContainer = Color(0xFF5C2222)
    val DarkOnErrorContainer = Color(0xFFFFD9D9)
}

val OldMoneyLightColorScheme: ColorScheme = lightColorScheme(
    primary = OldMoneyColors.LightDeepGreenPrimary,
    onPrimary = OldMoneyColors.LightOnPrimary,
    primaryContainer = OldMoneyColors.LightBrassPrimaryContainer,
    onPrimaryContainer = OldMoneyColors.LightOnBrassContainer,
    secondary = OldMoneyColors.LightAntiqueBrassSecondary,
    onSecondary = OldMoneyColors.LightOnSecondary,
    secondaryContainer = OldMoneyColors.LightSandSecondaryContainer,
    onSecondaryContainer = OldMoneyColors.LightOnSecondaryContainer,
    tertiary = OldMoneyColors.LightEmeraldTertiary,
    onTertiary = OldMoneyColors.LightOnTertiary,
    tertiaryContainer = OldMoneyColors.LightMistTertiaryContainer,
    onTertiaryContainer = OldMoneyColors.LightOnTertiaryContainer,
    background = OldMoneyColors.LightIvoryBackground,
    onBackground = OldMoneyColors.LightGreenInkText,
    surface = OldMoneyColors.LightVellumSurface,
    onSurface = OldMoneyColors.LightGreenInkText,
    surfaceVariant = OldMoneyColors.LightChampagneSurfaceVariant,
    onSurfaceVariant = OldMoneyColors.LightMutedText,
    outline = OldMoneyColors.LightBrassOutline,
    outlineVariant = OldMoneyColors.LightSoftOutlineVariant,
    error = OldMoneyColors.LightError,
    onError = OldMoneyColors.LightOnError,
    errorContainer = OldMoneyColors.LightErrorContainer,
    onErrorContainer = OldMoneyColors.LightOnErrorContainer,
    surfaceBright = OldMoneyColors.LightVellumSurface,
    surfaceDim = OldMoneyColors.LightChampagneSurfaceVariant,
    surfaceContainerLowest = OldMoneyColors.LightVellumSurface,
    surfaceContainerLow = OldMoneyColors.LightVellumSurface,
    surfaceContainer = OldMoneyColors.LightChampagneSurfaceVariant,
    surfaceContainerHigh = OldMoneyColors.LightChampagneSurfaceVariant,
    surfaceContainerHighest = OldMoneyColors.LightChampagneSurfaceVariant
)

val OldMoneyDarkColorScheme: ColorScheme = darkColorScheme(
    primary = OldMoneyColors.DarkBrassPrimary,
    onPrimary = OldMoneyColors.DarkOnPrimary,
    primaryContainer = OldMoneyColors.DarkBronzePrimaryContainer,
    onPrimaryContainer = OldMoneyColors.DarkOnPrimaryContainer,
    secondary = OldMoneyColors.DarkChampagneSecondary,
    onSecondary = OldMoneyColors.DarkOnSecondary,
    secondaryContainer = OldMoneyColors.DarkUmberSecondaryContainer,
    onSecondaryContainer = OldMoneyColors.DarkOnSecondaryContainer,
    tertiary = OldMoneyColors.DarkSageTertiary,
    onTertiary = OldMoneyColors.DarkOnTertiary,
    tertiaryContainer = OldMoneyColors.DarkPineTertiaryContainer,
    onTertiaryContainer = OldMoneyColors.DarkOnTertiaryContainer,
    background = OldMoneyColors.DarkGreenBackground,
    onBackground = OldMoneyColors.DarkCreamText,
    surface = OldMoneyColors.DarkGreenSurface,
    onSurface = OldMoneyColors.DarkCreamText,
    surfaceVariant = OldMoneyColors.DarkGreenSurfaceVariant,
    onSurfaceVariant = OldMoneyColors.DarkMutedText,
    outline = OldMoneyColors.DarkBrassOutline,
    outlineVariant = OldMoneyColors.DarkSoftOutlineVariant,
    error = OldMoneyColors.DarkError,
    onError = OldMoneyColors.DarkOnError,
    errorContainer = OldMoneyColors.DarkErrorContainer,
    onErrorContainer = OldMoneyColors.DarkOnErrorContainer,
    surfaceBright = OldMoneyColors.DarkGreenSurfaceVariant,
    surfaceDim = OldMoneyColors.DarkGreenSurface,
    surfaceContainerLowest = OldMoneyColors.DarkGreenSurface,
    surfaceContainerLow = OldMoneyColors.DarkGreenSurface,
    surfaceContainer = OldMoneyColors.DarkGreenSurfaceVariant,
    surfaceContainerHigh = OldMoneyColors.DarkGreenSurfaceVariant,
    surfaceContainerHighest = OldMoneyColors.DarkGreenSurfaceVariant
)

val NeubrutalismLightColorScheme: ColorScheme = lightColorScheme(
    primary = NeubrutalismColors.LightInkPrimary,
    onPrimary = NeubrutalismColors.LightOnPrimary,
    primaryContainer = NeubrutalismColors.LightCandyYellowContainer,
    onPrimaryContainer = NeubrutalismColors.LightOnYellowContainer,
    secondary = NeubrutalismColors.LightSignalRedSecondary,
    onSecondary = NeubrutalismColors.LightOnSecondary,
    secondaryContainer = NeubrutalismColors.LightBlushSecondaryContainer,
    onSecondaryContainer = NeubrutalismColors.LightOnSecondaryContainer,
    tertiary = NeubrutalismColors.LightSignalBlueTertiary,
    onTertiary = NeubrutalismColors.LightOnTertiary,
    tertiaryContainer = NeubrutalismColors.LightSkyTertiaryContainer,
    onTertiaryContainer = NeubrutalismColors.LightOnTertiaryContainer,
    background = NeubrutalismColors.LightPaperBackground,
    onBackground = NeubrutalismColors.LightInkText,
    surface = NeubrutalismColors.LightCardSurface,
    onSurface = NeubrutalismColors.LightInkText,
    surfaceVariant = NeubrutalismColors.LightCreamSurfaceVariant,
    onSurfaceVariant = NeubrutalismColors.LightMutedText,
    outline = NeubrutalismColors.LightInkOutline,
    outlineVariant = NeubrutalismColors.LightSoftOutlineVariant,
    error = NeubrutalismColors.LightError,
    onError = NeubrutalismColors.LightOnError,
    errorContainer = NeubrutalismColors.LightErrorContainer,
    onErrorContainer = NeubrutalismColors.LightOnErrorContainer,
    surfaceBright = NeubrutalismColors.LightCardSurface,
    surfaceDim = NeubrutalismColors.LightCreamSurfaceVariant,
    surfaceContainerLowest = NeubrutalismColors.LightCardSurface,
    surfaceContainerLow = NeubrutalismColors.LightCardSurface,
    surfaceContainer = NeubrutalismColors.LightCreamSurfaceVariant,
    surfaceContainerHigh = NeubrutalismColors.LightCreamSurfaceVariant,
    surfaceContainerHighest = NeubrutalismColors.LightCreamSurfaceVariant
)

val NeubrutalismDarkColorScheme: ColorScheme = darkColorScheme(
    primary = NeubrutalismColors.DarkCandyYellowPrimary,
    onPrimary = NeubrutalismColors.DarkOnPrimary,
    primaryContainer = NeubrutalismColors.DarkOlivePrimaryContainer,
    onPrimaryContainer = NeubrutalismColors.DarkOnPrimaryContainer,
    secondary = NeubrutalismColors.DarkSignalRedSecondary,
    onSecondary = NeubrutalismColors.DarkOnSecondary,
    secondaryContainer = NeubrutalismColors.DarkMaroonSecondaryContainer,
    onSecondaryContainer = NeubrutalismColors.DarkOnSecondaryContainer,
    tertiary = NeubrutalismColors.DarkSignalBlueTertiary,
    onTertiary = NeubrutalismColors.DarkOnTertiary,
    tertiaryContainer = NeubrutalismColors.DarkNavyTertiaryContainer,
    onTertiaryContainer = NeubrutalismColors.DarkOnTertiaryContainer,
    background = NeubrutalismColors.DarkCoalBackground,
    onBackground = NeubrutalismColors.DarkCreamText,
    surface = NeubrutalismColors.DarkCardSurface,
    onSurface = NeubrutalismColors.DarkCreamText,
    surfaceVariant = NeubrutalismColors.DarkSlateSurfaceVariant,
    onSurfaceVariant = NeubrutalismColors.DarkMutedText,
    outline = NeubrutalismColors.DarkCreamOutline,
    outlineVariant = NeubrutalismColors.DarkSoftOutlineVariant,
    error = NeubrutalismColors.DarkError,
    onError = NeubrutalismColors.DarkOnError,
    errorContainer = NeubrutalismColors.DarkErrorContainer,
    onErrorContainer = NeubrutalismColors.DarkOnErrorContainer,
    surfaceBright = NeubrutalismColors.DarkSlateSurfaceVariant,
    surfaceDim = NeubrutalismColors.DarkCardSurface,
    surfaceContainerLowest = NeubrutalismColors.DarkCardSurface,
    surfaceContainerLow = NeubrutalismColors.DarkCardSurface,
    surfaceContainer = NeubrutalismColors.DarkSlateSurfaceVariant,
    surfaceContainerHigh = NeubrutalismColors.DarkSlateSurfaceVariant,
    surfaceContainerHighest = NeubrutalismColors.DarkSlateSurfaceVariant
)

/**
 * Shared palette definition for Android View system (OverlayService).
 */
data class OverlayPalette(
    val surfaceColor: Int,
    val surfaceVariantColor: Int,
    val primaryColor: Int,
    val primaryContainerColor: Int,
    val onPrimaryContainerColor: Int,
    val textColor: Int,
    val mutedTextColor: Int,
    val outlineColor: Int,
    val accentColor: Int,
    val selectedChipContainerColor: Int,
    val selectedChipContentColor: Int,
    val selectedChipStrokeColor: Int?,
    val isDark: Boolean,
    val visualTheme: AppVisualTheme
)

object ThemePaletteResolver {

    /**
     * Resolves an [OverlayPalette] for the given visual theme and dark mode flag.
     * Compatible with Android Views in [OverlayService].
     */
    fun resolveOverlayPalette(
        context: Context,
        visualTheme: AppVisualTheme,
        isDark: Boolean
    ): OverlayPalette {
        val base = when (visualTheme) {
            AppVisualTheme.AURORA -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = AuroraColors.DarkNightSurface.toArgb(),
                        surfaceVariantColor = AuroraColors.DarkNightSurfaceVariant.toArgb(),
                        primaryColor = AuroraColors.DarkSkyPrimary.toArgb(),
                        primaryContainerColor = AuroraColors.DarkNavyPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = AuroraColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = AuroraColors.DarkStarText.toArgb(),
                        mutedTextColor = AuroraColors.DarkMutedText.toArgb(),
                        outlineColor = AuroraColors.DarkIrisOutline.toArgb(),
                        accentColor = AuroraColors.DarkMagentaSecondary.toArgb(),
                        selectedChipContainerColor = AuroraColors.DarkNavyPrimaryContainer.toArgb(),
                        selectedChipContentColor = AuroraColors.DarkOnPrimaryContainer.toArgb(),
                        selectedChipStrokeColor = AuroraColors.DarkSkyPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = AuroraColors.LightCloudSurface.toArgb(),
                        surfaceVariantColor = AuroraColors.LightMistSurfaceVariant.toArgb(),
                        primaryColor = AuroraColors.LightAuroraBluePrimary.toArgb(),
                        primaryContainerColor = AuroraColors.LightSkyPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = AuroraColors.LightOnSkyContainer.toArgb(),
                        textColor = AuroraColors.LightNightText.toArgb(),
                        mutedTextColor = AuroraColors.LightMutedText.toArgb(),
                        outlineColor = AuroraColors.LightIrisOutline.toArgb(),
                        accentColor = AuroraColors.LightMagentaSecondary.toArgb(),
                        selectedChipContainerColor = AuroraColors.LightSkyPrimaryContainer.toArgb(),
                        selectedChipContentColor = AuroraColors.LightOnSkyContainer.toArgb(),
                        selectedChipStrokeColor = AuroraColors.LightAuroraBluePrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.SYNTHWAVE -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = SynthwaveColors.DarkDeckSurface.toArgb(),
                        surfaceVariantColor = SynthwaveColors.DarkDeckSurfaceVariant.toArgb(),
                        primaryColor = SynthwaveColors.DarkNeonBluePrimary.toArgb(),
                        primaryContainerColor = SynthwaveColors.DarkAbyssPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = SynthwaveColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = SynthwaveColors.DarkStaticText.toArgb(),
                        mutedTextColor = SynthwaveColors.DarkMutedText.toArgb(),
                        outlineColor = SynthwaveColors.DarkNeonOutline.toArgb(),
                        accentColor = SynthwaveColors.DarkHotPinkSecondary.toArgb(),
                        selectedChipContainerColor = SynthwaveColors.DarkNeonBluePrimary.toArgb(),
                        selectedChipContentColor = SynthwaveColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = SynthwaveColors.DarkNeonBluePrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = SynthwaveColors.LightChromeSurface.toArgb(),
                        surfaceVariantColor = SynthwaveColors.LightHazeSurfaceVariant.toArgb(),
                        primaryColor = SynthwaveColors.LightUltravioletPrimary.toArgb(),
                        primaryContainerColor = SynthwaveColors.LightLavenderPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = SynthwaveColors.LightOnLavenderContainer.toArgb(),
                        textColor = SynthwaveColors.LightInkText.toArgb(),
                        mutedTextColor = SynthwaveColors.LightMutedText.toArgb(),
                        outlineColor = SynthwaveColors.LightNeonOutline.toArgb(),
                        accentColor = SynthwaveColors.LightHotPinkSecondary.toArgb(),
                        selectedChipContainerColor = SynthwaveColors.LightLavenderPrimaryContainer.toArgb(),
                        selectedChipContentColor = SynthwaveColors.LightOnLavenderContainer.toArgb(),
                        selectedChipStrokeColor = SynthwaveColors.LightUltravioletPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.GATSBY -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = GatsbyColors.DarkVelvetSurface.toArgb(),
                        surfaceVariantColor = GatsbyColors.DarkVelvetSurfaceVariant.toArgb(),
                        primaryColor = GatsbyColors.DarkGoldPrimary.toArgb(),
                        primaryContainerColor = GatsbyColors.DarkBronzePrimaryContainer.toArgb(),
                        onPrimaryContainerColor = GatsbyColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = GatsbyColors.DarkCreamText.toArgb(),
                        mutedTextColor = GatsbyColors.DarkMutedText.toArgb(),
                        outlineColor = GatsbyColors.DarkGoldOutline.toArgb(),
                        accentColor = GatsbyColors.DarkFoilSecondary.toArgb(),
                        selectedChipContainerColor = GatsbyColors.DarkGoldPrimary.toArgb(),
                        selectedChipContentColor = GatsbyColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = GatsbyColors.DarkGoldPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = GatsbyColors.LightBallroomSurface.toArgb(),
                        surfaceVariantColor = GatsbyColors.LightGiltSurfaceVariant.toArgb(),
                        primaryColor = GatsbyColors.LightTuxedoPrimary.toArgb(),
                        primaryContainerColor = GatsbyColors.LightGoldPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = GatsbyColors.LightOnGoldContainer.toArgb(),
                        textColor = GatsbyColors.LightInkText.toArgb(),
                        mutedTextColor = GatsbyColors.LightMutedText.toArgb(),
                        outlineColor = GatsbyColors.LightGoldOutline.toArgb(),
                        accentColor = GatsbyColors.LightBronzeSecondary.toArgb(),
                        selectedChipContainerColor = GatsbyColors.LightGoldPrimaryContainer.toArgb(),
                        selectedChipContentColor = GatsbyColors.LightOnGoldContainer.toArgb(),
                        selectedChipStrokeColor = GatsbyColors.LightTuxedoPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.UKIYO -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = UkiyoColors.DarkDeepSurface.toArgb(),
                        surfaceVariantColor = UkiyoColors.DarkDeepSurfaceVariant.toArgb(),
                        primaryColor = UkiyoColors.DarkMoonPrimary.toArgb(),
                        primaryContainerColor = UkiyoColors.DarkIndigoPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = UkiyoColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = UkiyoColors.DarkPaperText.toArgb(),
                        mutedTextColor = UkiyoColors.DarkMutedText.toArgb(),
                        outlineColor = UkiyoColors.DarkMistOutline.toArgb(),
                        accentColor = UkiyoColors.DarkVermilionSecondary.toArgb(),
                        selectedChipContainerColor = UkiyoColors.DarkMoonPrimary.toArgb(),
                        selectedChipContentColor = UkiyoColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = UkiyoColors.DarkMoonPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = UkiyoColors.LightRiceSurface.toArgb(),
                        surfaceVariantColor = UkiyoColors.LightSandSurfaceVariant.toArgb(),
                        primaryColor = UkiyoColors.LightSumiPrimary.toArgb(),
                        primaryContainerColor = UkiyoColors.LightWavePrimaryContainer.toArgb(),
                        onPrimaryContainerColor = UkiyoColors.LightOnWaveContainer.toArgb(),
                        textColor = UkiyoColors.LightInkText.toArgb(),
                        mutedTextColor = UkiyoColors.LightMutedText.toArgb(),
                        outlineColor = UkiyoColors.LightSumiOutline.toArgb(),
                        accentColor = UkiyoColors.LightVermilionSecondary.toArgb(),
                        selectedChipContainerColor = UkiyoColors.LightWavePrimaryContainer.toArgb(),
                        selectedChipContentColor = UkiyoColors.LightOnWaveContainer.toArgb(),
                        selectedChipStrokeColor = UkiyoColors.LightSumiPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.PIXEL -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = PixelColors.DarkCabinetSurface.toArgb(),
                        surfaceVariantColor = PixelColors.DarkCabinetSurfaceVariant.toArgb(),
                        primaryColor = PixelColors.DarkPhosphorPrimary.toArgb(),
                        primaryContainerColor = PixelColors.DarkDeepPhosphorContainer.toArgb(),
                        onPrimaryContainerColor = PixelColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = PixelColors.DarkPhosphorText.toArgb(),
                        mutedTextColor = PixelColors.DarkMutedText.toArgb(),
                        outlineColor = PixelColors.DarkPhosphorOutline.toArgb(),
                        accentColor = PixelColors.DarkAmberSecondary.toArgb(),
                        selectedChipContainerColor = PixelColors.DarkPhosphorPrimary.toArgb(),
                        selectedChipContentColor = PixelColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = PixelColors.DarkPhosphorPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = PixelColors.LightCartridgeSurface.toArgb(),
                        surfaceVariantColor = PixelColors.LightSandSurfaceVariant.toArgb(),
                        primaryColor = PixelColors.LightPhosphorDeepPrimary.toArgb(),
                        primaryContainerColor = PixelColors.LightPhosphorPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = PixelColors.LightOnPhosphorContainer.toArgb(),
                        textColor = PixelColors.LightCabinetText.toArgb(),
                        mutedTextColor = PixelColors.LightMutedText.toArgb(),
                        outlineColor = PixelColors.LightJoystickOutline.toArgb(),
                        accentColor = PixelColors.LightCoinSlotSecondary.toArgb(),
                        selectedChipContainerColor = PixelColors.LightPhosphorPrimaryContainer.toArgb(),
                        selectedChipContentColor = PixelColors.LightOnPhosphorContainer.toArgb(),
                        selectedChipStrokeColor = PixelColors.LightPhosphorDeepPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.KAWAII -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = KawaiiColors.DarkGrapeSurface.toArgb(),
                        surfaceVariantColor = KawaiiColors.DarkGrapeSurfaceVariant.toArgb(),
                        primaryColor = KawaiiColors.DarkCandyPrimary.toArgb(),
                        primaryContainerColor = KawaiiColors.DarkPlumPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = KawaiiColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = KawaiiColors.DarkSugarText.toArgb(),
                        mutedTextColor = KawaiiColors.DarkMutedText.toArgb(),
                        outlineColor = KawaiiColors.DarkRoseOutline.toArgb(),
                        accentColor = KawaiiColors.DarkSkySecondary.toArgb(),
                        selectedChipContainerColor = KawaiiColors.DarkCandyPrimary.toArgb(),
                        selectedChipContentColor = KawaiiColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = KawaiiColors.DarkCandyPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = KawaiiColors.LightCreamSurface.toArgb(),
                        surfaceVariantColor = KawaiiColors.LightBlushSurfaceVariant.toArgb(),
                        primaryColor = KawaiiColors.LightGrapePrimary.toArgb(),
                        primaryContainerColor = KawaiiColors.LightPetalPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = KawaiiColors.LightOnPetalContainer.toArgb(),
                        textColor = KawaiiColors.LightCocoaText.toArgb(),
                        mutedTextColor = KawaiiColors.LightMutedText.toArgb(),
                        outlineColor = KawaiiColors.LightRoseOutline.toArgb(),
                        accentColor = KawaiiColors.LightSkySecondary.toArgb(),
                        selectedChipContainerColor = KawaiiColors.LightPetalPrimaryContainer.toArgb(),
                        selectedChipContentColor = KawaiiColors.LightOnPetalContainer.toArgb(),
                        selectedChipStrokeColor = KawaiiColors.LightGrapePrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.SOLARPUNK -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = SolarpunkColors.DarkCanopySurface.toArgb(),
                        surfaceVariantColor = SolarpunkColors.DarkCanopySurfaceVariant.toArgb(),
                        primaryColor = SolarpunkColors.DarkSproutPrimary.toArgb(),
                        primaryContainerColor = SolarpunkColors.DarkGrovePrimaryContainer.toArgb(),
                        onPrimaryContainerColor = SolarpunkColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = SolarpunkColors.DarkDewText.toArgb(),
                        mutedTextColor = SolarpunkColors.DarkMutedText.toArgb(),
                        outlineColor = SolarpunkColors.DarkVineOutline.toArgb(),
                        accentColor = SolarpunkColors.DarkSolarSecondary.toArgb(),
                        selectedChipContainerColor = SolarpunkColors.DarkSproutPrimary.toArgb(),
                        selectedChipContentColor = SolarpunkColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = SolarpunkColors.DarkSproutPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = SolarpunkColors.LightCanopySurface.toArgb(),
                        surfaceVariantColor = SolarpunkColors.LightHaySurfaceVariant.toArgb(),
                        primaryColor = SolarpunkColors.LightVerdantPrimary.toArgb(),
                        primaryContainerColor = SolarpunkColors.LightLeafPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = SolarpunkColors.LightOnLeafContainer.toArgb(),
                        textColor = SolarpunkColors.LightSoilText.toArgb(),
                        mutedTextColor = SolarpunkColors.LightMutedText.toArgb(),
                        outlineColor = SolarpunkColors.LightVineOutline.toArgb(),
                        accentColor = SolarpunkColors.LightHarvestSecondary.toArgb(),
                        selectedChipContainerColor = SolarpunkColors.LightLeafPrimaryContainer.toArgb(),
                        selectedChipContentColor = SolarpunkColors.LightOnLeafContainer.toArgb(),
                        selectedChipStrokeColor = SolarpunkColors.LightVerdantPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.NOIR -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = NoirColors.DarkOfficeSurface.toArgb(),
                        surfaceVariantColor = NoirColors.DarkOfficeSurfaceVariant.toArgb(),
                        primaryColor = NoirColors.DarkFogPrimary.toArgb(),
                        primaryContainerColor = NoirColors.DarkSmokePrimaryContainer.toArgb(),
                        onPrimaryContainerColor = NoirColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = NoirColors.DarkFogText.toArgb(),
                        mutedTextColor = NoirColors.DarkMutedText.toArgb(),
                        outlineColor = NoirColors.DarkSmokeOutline.toArgb(),
                        accentColor = NoirColors.DarkStreetlampSecondary.toArgb(),
                        selectedChipContainerColor = NoirColors.DarkStreetlampSecondary.toArgb(),
                        selectedChipContentColor = NoirColors.DarkOnSecondary.toArgb(),
                        selectedChipStrokeColor = NoirColors.DarkStreetlampSecondary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = NoirColors.LightCaseSurface.toArgb(),
                        surfaceVariantColor = NoirColors.LightManilaSurfaceVariant.toArgb(),
                        primaryColor = NoirColors.LightInkPrimary.toArgb(),
                        primaryContainerColor = NoirColors.LightFogPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = NoirColors.LightOnFogContainer.toArgb(),
                        textColor = NoirColors.LightInkText.toArgb(),
                        mutedTextColor = NoirColors.LightMutedText.toArgb(),
                        outlineColor = NoirColors.LightSmokeOutline.toArgb(),
                        accentColor = NoirColors.LightStreetlampSecondary.toArgb(),
                        selectedChipContainerColor = NoirColors.LightFogPrimaryContainer.toArgb(),
                        selectedChipContentColor = NoirColors.LightOnFogContainer.toArgb(),
                        selectedChipStrokeColor = NoirColors.LightInkPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.GLASS -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = GlassColors.DarkPaneSurface.toArgb(),
                        surfaceVariantColor = GlassColors.DarkPaneSurfaceVariant.toArgb(),
                        primaryColor = GlassColors.DarkBeamPrimary.toArgb(),
                        primaryContainerColor = GlassColors.DarkHarborPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = GlassColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = GlassColors.DarkFrostText.toArgb(),
                        mutedTextColor = GlassColors.DarkMutedText.toArgb(),
                        outlineColor = GlassColors.DarkGlassOutline.toArgb(),
                        accentColor = GlassColors.DarkIrisSecondary.toArgb(),
                        selectedChipContainerColor = GlassColors.DarkBeamPrimary.toArgb(),
                        selectedChipContentColor = GlassColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = GlassColors.DarkBeamPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = GlassColors.LightPaneSurface.toArgb(),
                        surfaceVariantColor = GlassColors.LightMistSurfaceVariant.toArgb(),
                        primaryColor = GlassColors.LightCobaltPrimary.toArgb(),
                        primaryContainerColor = GlassColors.LightIcePrimaryContainer.toArgb(),
                        onPrimaryContainerColor = GlassColors.LightOnIceContainer.toArgb(),
                        textColor = GlassColors.LightInkText.toArgb(),
                        mutedTextColor = GlassColors.LightMutedText.toArgb(),
                        outlineColor = GlassColors.LightGlassOutline.toArgb(),
                        accentColor = GlassColors.LightIrisSecondary.toArgb(),
                        selectedChipContainerColor = GlassColors.LightIcePrimaryContainer.toArgb(),
                        selectedChipContentColor = GlassColors.LightOnIceContainer.toArgb(),
                        selectedChipStrokeColor = GlassColors.LightCobaltPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.NOUVEAU -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = NouveauColors.DarkLagoonSurface.toArgb(),
                        surfaceVariantColor = NouveauColors.DarkLagoonSurfaceVariant.toArgb(),
                        primaryColor = NouveauColors.DarkSeafoamPrimary.toArgb(),
                        primaryContainerColor = NouveauColors.DarkReefPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = NouveauColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = NouveauColors.DarkIvoryText.toArgb(),
                        mutedTextColor = NouveauColors.DarkMutedText.toArgb(),
                        outlineColor = NouveauColors.DarkGoldOutline.toArgb(),
                        accentColor = NouveauColors.DarkGoldSecondary.toArgb(),
                        selectedChipContainerColor = NouveauColors.DarkSeafoamPrimary.toArgb(),
                        selectedChipContentColor = NouveauColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = NouveauColors.DarkSeafoamPrimary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = NouveauColors.LightGallerySurface.toArgb(),
                        surfaceVariantColor = NouveauColors.LightParchmentSurfaceVariant.toArgb(),
                        primaryColor = NouveauColors.LightDeepTealPrimary.toArgb(),
                        primaryContainerColor = NouveauColors.LightLagoonPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = NouveauColors.LightOnLagoonContainer.toArgb(),
                        textColor = NouveauColors.LightInkText.toArgb(),
                        mutedTextColor = NouveauColors.LightMutedText.toArgb(),
                        outlineColor = NouveauColors.LightGoldOutline.toArgb(),
                        accentColor = NouveauColors.LightOldGoldSecondary.toArgb(),
                        selectedChipContainerColor = NouveauColors.LightLagoonPrimaryContainer.toArgb(),
                        selectedChipContentColor = NouveauColors.LightOnLagoonContainer.toArgb(),
                        selectedChipStrokeColor = NouveauColors.LightDeepTealPrimary.toArgb(),
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.COTTAGE -> {
                if (isDark) {
                    val primary = CottageColors.DarkFadedRosePrimary.toArgb()
                    val container = CottageColors.DarkMauvePrimaryContainer.toArgb()
                    val onContainer = CottageColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = CottageColors.DarkPlumSurface.toArgb(),
                        surfaceVariantColor = CottageColors.DarkPlumSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = CottageColors.DarkCreamText.toArgb(),
                        mutedTextColor = CottageColors.DarkMutedText.toArgb(),
                        outlineColor = CottageColors.DarkRoseGlowOutline.toArgb(),
                        accentColor = CottageColors.DarkSageSecondary.toArgb(),
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = CottageColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = CottageColors.LightRosewoodPrimary.toArgb()
                    val container = CottageColors.LightFadedRoseContainer.toArgb()
                    val onContainer = CottageColors.LightOnRoseContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = CottageColors.LightCottageSurface.toArgb(),
                        surfaceVariantColor = CottageColors.LightLinenSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = CottageColors.LightUmberText.toArgb(),
                        mutedTextColor = CottageColors.LightMutedText.toArgb(),
                        outlineColor = CottageColors.LightRoseTaupeOutline.toArgb(),
                        accentColor = CottageColors.LightSageSecondary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.STARBASE -> {
                if (isDark) {
                    val primary = StarbaseColors.DarkPeachPrimary.toArgb()
                    val container = StarbaseColors.DarkWinePrimaryContainer.toArgb()
                    val onContainer = StarbaseColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = StarbaseColors.DarkOxbloodSurface.toArgb(),
                        surfaceVariantColor = StarbaseColors.DarkOxbloodSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = StarbaseColors.DarkCreamText.toArgb(),
                        mutedTextColor = StarbaseColors.DarkMutedText.toArgb(),
                        outlineColor = StarbaseColors.DarkPeachOutline.toArgb(),
                        accentColor = StarbaseColors.DarkAmberSecondary.toArgb(),
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = StarbaseColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = StarbaseColors.LightOxbloodPrimary.toArgb()
                    val container = StarbaseColors.LightPeachPrimaryContainer.toArgb()
                    val onContainer = StarbaseColors.LightOnPeachContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = StarbaseColors.LightFlightSurface.toArgb(),
                        surfaceVariantColor = StarbaseColors.LightPeachSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = StarbaseColors.LightCockpitText.toArgb(),
                        mutedTextColor = StarbaseColors.LightMutedText.toArgb(),
                        outlineColor = StarbaseColors.LightCopperOutline.toArgb(),
                        accentColor = StarbaseColors.LightEmberSecondary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.ATELIER -> {
                if (isDark) {
                    val primary = AtelierColors.DarkEspressoPrimary.toArgb()
                    val container = AtelierColors.DarkEspressoPrimaryContainer.toArgb()
                    val onContainer = AtelierColors.DarkOnEspressoContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = AtelierColors.DarkEmberSurface.toArgb(),
                        surfaceVariantColor = AtelierColors.DarkEmberSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = AtelierColors.DarkCreamText.toArgb(),
                        mutedTextColor = AtelierColors.DarkMutedText.toArgb(),
                        outlineColor = AtelierColors.DarkCreamOutline.toArgb(),
                        accentColor = AtelierColors.DarkCreamSecondary.toArgb(),
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = AtelierColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = AtelierColors.LightFiredTerracottaPrimary.toArgb()
                    val container = AtelierColors.LightPaleTerracottaContainer.toArgb()
                    val onContainer = AtelierColors.LightOnTerracottaContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = AtelierColors.LightGallerySurface.toArgb(),
                        surfaceVariantColor = AtelierColors.LightSandSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = AtelierColors.LightEspressoText.toArgb(),
                        mutedTextColor = AtelierColors.LightMutedText.toArgb(),
                        outlineColor = AtelierColors.LightClayOutline.toArgb(),
                        accentColor = AtelierColors.LightFiredTerracottaPrimary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.OLD_MONEY -> {
                if (isDark) {
                    val primary = OldMoneyColors.DarkBrassPrimary.toArgb()
                    val container = OldMoneyColors.DarkBronzePrimaryContainer.toArgb()
                    val onContainer = OldMoneyColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = OldMoneyColors.DarkGreenSurface.toArgb(),
                        surfaceVariantColor = OldMoneyColors.DarkGreenSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = OldMoneyColors.DarkCreamText.toArgb(),
                        mutedTextColor = OldMoneyColors.DarkMutedText.toArgb(),
                        outlineColor = OldMoneyColors.DarkBrassOutline.toArgb(),
                        accentColor = OldMoneyColors.DarkChampagneSecondary.toArgb(),
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = OldMoneyColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = OldMoneyColors.LightDeepGreenPrimary.toArgb()
                    val container = OldMoneyColors.LightBrassPrimaryContainer.toArgb()
                    val onContainer = OldMoneyColors.LightOnBrassContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = OldMoneyColors.LightVellumSurface.toArgb(),
                        surfaceVariantColor = OldMoneyColors.LightChampagneSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = OldMoneyColors.LightGreenInkText.toArgb(),
                        mutedTextColor = OldMoneyColors.LightMutedText.toArgb(),
                        outlineColor = OldMoneyColors.LightBrassOutline.toArgb(),
                        accentColor = OldMoneyColors.LightAntiqueBrassSecondary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.NEUBRUTALISM -> {
                if (isDark) {
                    val primary = NeubrutalismColors.DarkCandyYellowPrimary.toArgb()
                    val container = NeubrutalismColors.DarkOlivePrimaryContainer.toArgb()
                    val onContainer = NeubrutalismColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = NeubrutalismColors.DarkCardSurface.toArgb(),
                        surfaceVariantColor = NeubrutalismColors.DarkSlateSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = NeubrutalismColors.DarkCreamText.toArgb(),
                        mutedTextColor = NeubrutalismColors.DarkMutedText.toArgb(),
                        outlineColor = NeubrutalismColors.DarkCreamOutline.toArgb(),
                        accentColor = NeubrutalismColors.DarkSignalRedSecondary.toArgb(),
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = NeubrutalismColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = NeubrutalismColors.LightInkPrimary.toArgb()
                    val container = NeubrutalismColors.LightCandyYellowContainer.toArgb()
                    val onContainer = NeubrutalismColors.LightOnYellowContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = NeubrutalismColors.LightCardSurface.toArgb(),
                        surfaceVariantColor = NeubrutalismColors.LightCreamSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = NeubrutalismColors.LightInkText.toArgb(),
                        mutedTextColor = NeubrutalismColors.LightMutedText.toArgb(),
                        outlineColor = NeubrutalismColors.LightInkOutline.toArgb(),
                        accentColor = NeubrutalismColors.LightSignalRedSecondary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.SKETCHBOOK -> {
                if (isDark) {
                    val primary = SketchbookColors.DarkPrimaryBlue.toArgb()
                    val container = SketchbookColors.DarkPrimaryContainer.toArgb()
                    val onContainer = SketchbookColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = SketchbookColors.DarkPaperSurface.toArgb(),
                        surfaceVariantColor = SketchbookColors.DarkPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = SketchbookColors.DarkMainPaperText.toArgb(),
                        mutedTextColor = SketchbookColors.DarkMutedPaperText.toArgb(),
                        outlineColor = SketchbookColors.DarkOutline.toArgb(),
                        accentColor = SketchbookColors.DarkRedAccent.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = SketchbookColors.LightNavyInkPrimary.toArgb()
                    val container = SketchbookColors.LightPrimaryContainer.toArgb()
                    val onContainer = SketchbookColors.LightOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = SketchbookColors.LightPaperSurface.toArgb(),
                        surfaceVariantColor = SketchbookColors.LightPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = SketchbookColors.LightNavyInkPrimary.toArgb(),
                        mutedTextColor = SketchbookColors.LightMutedGraphite.toArgb(),
                        outlineColor = SketchbookColors.LightOutline.toArgb(),
                        accentColor = SketchbookColors.LightMutedRedAccent.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.HERBARIUM -> {
                if (isDark) {
                    val primary = BotanicalColors.DarkSagePrimary.toArgb()
                    val container = BotanicalColors.DarkPrimaryContainer.toArgb()
                    val onContainer = BotanicalColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = BotanicalColors.DarkPaperSurface.toArgb(),
                        surfaceVariantColor = BotanicalColors.DarkPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = BotanicalColors.DarkParchmentText.toArgb(),
                        mutedTextColor = BotanicalColors.DarkMutedText.toArgb(),
                        outlineColor = BotanicalColors.DarkOutline.toArgb(),
                        accentColor = BotanicalColors.DarkMutedRoseTertiary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = BotanicalColors.LightLeafGreenPrimary.toArgb()
                    val container = BotanicalColors.LightSagePrimaryContainer.toArgb()
                    val onContainer = BotanicalColors.LightOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = BotanicalColors.LightWarmPaperSurface.toArgb(),
                        surfaceVariantColor = BotanicalColors.LightPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = BotanicalColors.LightBotanicalInkText.toArgb(),
                        mutedTextColor = BotanicalColors.LightMutedInkText.toArgb(),
                        outlineColor = BotanicalColors.LightOutline.toArgb(),
                        accentColor = BotanicalColors.LightMutedTerracottaTertiary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.DEFAULT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val primary = context.getColor(
                        if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
                    )
                    val surface = context.getColor(
                        if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
                    )
                    val surfaceVariant = context.getColor(
                        if (isDark) android.R.color.system_neutral2_800 else android.R.color.system_neutral2_100
                    )
                    val container = context.getColor(
                        if (isDark) android.R.color.system_accent1_700 else android.R.color.system_accent1_100
                    )
                    val onContainer = context.getColor(
                        if (isDark) android.R.color.system_accent1_100 else android.R.color.system_accent1_900
                    )
                    OverlayPalette(
                        surfaceColor = surface,
                        surfaceVariantColor = surfaceVariant,
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = if (isDark) 0xFFEDEDED.toInt() else 0xFF1A1C20.toInt(),
                        mutedTextColor = if (isDark) 0xFFC4C7D0.toInt() else 0xFF4A4E57.toInt(),
                        outlineColor = if (isDark) 0xFF4A4E57.toInt() else 0xFFC4C7D0.toInt(),
                        accentColor = primary,
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = android.graphics.Color.WHITE,
                        selectedChipStrokeColor = null,
                        isDark = isDark,
                        visualTheme = visualTheme
                    )
                } else {
                    if (isDark) {
                        OverlayPalette(
                            surfaceColor = NeutralSurfaceDark.toArgb(),
                            surfaceVariantColor = 0xFF262930.toInt(),
                            primaryColor = NeutralPrimaryDark.toArgb(),
                            primaryContainerColor = NeutralPrimaryContainerDark.toArgb(),
                            onPrimaryContainerColor = NeutralOnPrimaryContainerDark.toArgb(),
                            textColor = 0xFFEDEDED.toInt(),
                            mutedTextColor = 0xFFC4C7D0.toInt(),
                            outlineColor = 0xFF383E4B.toInt(),
                            accentColor = NeutralPrimaryDark.toArgb(),
                            selectedChipContainerColor = NeutralPrimaryDark.toArgb(),
                            selectedChipContentColor = android.graphics.Color.WHITE,
                            selectedChipStrokeColor = null,
                            isDark = true,
                            visualTheme = visualTheme
                        )
                    } else {
                        OverlayPalette(
                            surfaceColor = NeutralSurfaceLight.toArgb(),
                            surfaceVariantColor = 0xFFF0F2F5.toInt(),
                            primaryColor = NeutralPrimaryLight.toArgb(),
                            primaryContainerColor = NeutralPrimaryContainerLight.toArgb(),
                            onPrimaryContainerColor = NeutralOnPrimaryContainerLight.toArgb(),
                            textColor = 0xFF1A1C20.toInt(),
                            mutedTextColor = 0xFF4A4E57.toInt(),
                            outlineColor = 0xFFE2E6EE.toInt(),
                            accentColor = NeutralPrimaryLight.toArgb(),
                            selectedChipContainerColor = NeutralPrimaryLight.toArgb(),
                            selectedChipContentColor = android.graphics.Color.WHITE,
                            selectedChipStrokeColor = null,
                            isDark = false,
                            visualTheme = visualTheme
                        )
                    }
                }
            }
            AppVisualTheme.PRESSROOM -> {
                if (isDark) {
                    val primary = PressroomColors.DarkPeachPrimary.toArgb()
                    val container = PressroomColors.DarkRoastPrimaryContainer.toArgb()
                    val onContainer = PressroomColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = PressroomColors.DarkCocoaSurface.toArgb(),
                        surfaceVariantColor = PressroomColors.DarkCocoaSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = PressroomColors.DarkCreamText.toArgb(),
                        mutedTextColor = PressroomColors.DarkMutedText.toArgb(),
                        outlineColor = PressroomColors.DarkCaramelOutline.toArgb(),
                        accentColor = PressroomColors.DarkCaramelSecondary.toArgb(),
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = PressroomColors.DarkOnPrimary.toArgb(),
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = PressroomColors.LightCocoaPrimary.toArgb()
                    val container = PressroomColors.LightPeachPrimaryContainer.toArgb()
                    val onContainer = PressroomColors.LightOnPeachContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = PressroomColors.LightPaperSurface.toArgb(),
                        surfaceVariantColor = PressroomColors.LightPeachSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = PressroomColors.LightCocoaText.toArgb(),
                        mutedTextColor = PressroomColors.LightMutedText.toArgb(),
                        outlineColor = PressroomColors.LightCaramelOutline.toArgb(),
                        accentColor = PressroomColors.LightCaramelSecondary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
        }

        // The popup must wear the app background, not a generic card white:
        // override the surface pair from the active Material scheme so every
        // theme (light and dark) reads as one continuous canvas.
        if (visualTheme == AppVisualTheme.DEFAULT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val dynamic = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                return base.copy(
                    surfaceColor = dynamic.background.toArgb(),
                    surfaceVariantColor = dynamic.surfaceVariant.toArgb()
                )
            }
            return base
        }
        val scheme = when (visualTheme) {
            AppVisualTheme.HERBARIUM -> if (isDark) HerbariumDarkColorScheme else HerbariumLightColorScheme
            AppVisualTheme.SKETCHBOOK -> if (isDark) SketchbookDarkColorScheme else SketchbookLightColorScheme
            AppVisualTheme.NEUBRUTALISM -> if (isDark) NeubrutalismDarkColorScheme else NeubrutalismLightColorScheme
            AppVisualTheme.OLD_MONEY -> if (isDark) OldMoneyDarkColorScheme else OldMoneyLightColorScheme
            AppVisualTheme.PRESSROOM -> if (isDark) PressroomDarkColorScheme else PressroomLightColorScheme
            AppVisualTheme.ATELIER -> if (isDark) AtelierDarkColorScheme else AtelierLightColorScheme
            AppVisualTheme.STARBASE -> if (isDark) StarbaseDarkColorScheme else StarbaseLightColorScheme
            AppVisualTheme.COTTAGE -> if (isDark) CottageDarkColorScheme else CottageLightColorScheme
            AppVisualTheme.AURORA -> if (isDark) AuroraDarkColorScheme else AuroraLightColorScheme
            AppVisualTheme.SYNTHWAVE -> if (isDark) SynthwaveDarkColorScheme else SynthwaveLightColorScheme
            AppVisualTheme.GATSBY -> if (isDark) GatsbyDarkColorScheme else GatsbyLightColorScheme
            AppVisualTheme.UKIYO -> if (isDark) UkiyoDarkColorScheme else UkiyoLightColorScheme
            AppVisualTheme.PIXEL -> if (isDark) PixelDarkColorScheme else PixelLightColorScheme
            AppVisualTheme.KAWAII -> if (isDark) KawaiiDarkColorScheme else KawaiiLightColorScheme
            AppVisualTheme.SOLARPUNK -> if (isDark) SolarpunkDarkColorScheme else SolarpunkLightColorScheme
            AppVisualTheme.NOIR -> if (isDark) NoirDarkColorScheme else NoirLightColorScheme
            AppVisualTheme.GLASS -> if (isDark) GlassDarkColorScheme else GlassLightColorScheme
            AppVisualTheme.NOUVEAU -> if (isDark) NouveauDarkColorScheme else NouveauLightColorScheme
            AppVisualTheme.DEFAULT -> return base
        }
        return base.copy(
            surfaceColor = scheme.background.toArgb(),
            surfaceVariantColor = scheme.surfaceVariant.toArgb()
        )
    }
}

/**
 * Curated color tokens for the Pressroom theme ("Merriweather Typography" —
 * warm newsprint, peach #FFE3C3, cocoa #5B3A30).
 */
object PressroomColors {
    // Light Palette (morning edition: warm paper, cocoa ink, peach highlights)
    val LightNewsprintBackground = Color(0xFFFBF3E8)
    val LightPaperSurface = Color(0xFFFFFDF8)
    val LightPeachSurfaceVariant = Color(0xFFF1E3CE)
    val LightCocoaPrimary = Color(0xFF5B3A30)
    val LightOnPrimary = Color(0xFFFFFDF8)
    val LightPeachPrimaryContainer = Color(0xFFFFE3C3)
    val LightOnPeachContainer = Color(0xFF3A241C)
    val LightCaramelSecondary = Color(0xFF9A5B23)
    val LightOnSecondary = Color(0xFFFFFDF8)
    val LightCrustSecondaryContainer = Color(0xFFF5D9BC)
    val LightOnSecondaryContainer = Color(0xFF40220F)
    val LightOliveTertiary = Color(0xFF6E7F4E)
    val LightOnTertiary = Color(0xFFFFFDF8)
    val LightSageTertiaryContainer = Color(0xFFE2E6CC)
    val LightOnTertiaryContainer = Color(0xFF27300F)
    val LightCocoaText = Color(0xFF33231C)
    val LightMutedText = Color(0xFF7A6A5C)
    val LightCaramelOutline = Color(0xFFB08A5A)
    val LightSoftOutlineVariant = Color(0xFFE3D3BC)
    val LightError = Color(0xFFA63A32)
    val LightOnError = Color(0xFFFFFDF8)
    val LightErrorContainer = Color(0xFFF5D5D1)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette (evening reading room: cocoa walls, peach lamplight)
    val DarkCocoaBackground = Color(0xFF5B3A30)
    val DarkCocoaSurface = Color(0xFF68463B)
    val DarkCocoaSurfaceVariant = Color(0xFF755144)
    val DarkPeachPrimary = Color(0xFFFFE3C3)
    val DarkOnPrimary = Color(0xFF3A241C)
    val DarkRoastPrimaryContainer = Color(0xFF7A4E33)
    val DarkOnPrimaryContainer = Color(0xFFFFE9D2)
    val DarkCaramelSecondary = Color(0xFFE8B07D)
    val DarkOnSecondary = Color(0xFF3A241C)
    val DarkBarkSecondaryContainer = Color(0xFF5F3B26)
    val DarkOnSecondaryContainer = Color(0xFFF5D9BC)
    val DarkSageTertiary = Color(0xFFB9C49A)
    val DarkOnTertiary = Color(0xFF27300F)
    val DarkMossTertiaryContainer = Color(0xFF4A5232)
    val DarkOnTertiaryContainer = Color(0xFFE2E6CC)
    val DarkCreamText = Color(0xFFF7EBD8)
    val DarkMutedText = Color(0xFFCDB79E)
    val DarkCaramelOutline = Color(0xFFC69A67)
    val DarkSoftOutlineVariant = Color(0xFF7A5A40)
    val DarkError = Color(0xFFE89890)
    val DarkOnError = Color(0xFF3A0E0B)
    val DarkErrorContainer = Color(0xFF5C2222)
    val DarkOnErrorContainer = Color(0xFFFFD9D9)
}

val PressroomLightColorScheme: ColorScheme = lightColorScheme(
    primary = PressroomColors.LightCocoaPrimary,
    onPrimary = PressroomColors.LightOnPrimary,
    primaryContainer = PressroomColors.LightPeachPrimaryContainer,
    onPrimaryContainer = PressroomColors.LightOnPeachContainer,
    secondary = PressroomColors.LightCaramelSecondary,
    onSecondary = PressroomColors.LightOnSecondary,
    secondaryContainer = PressroomColors.LightCrustSecondaryContainer,
    onSecondaryContainer = PressroomColors.LightOnSecondaryContainer,
    tertiary = PressroomColors.LightOliveTertiary,
    onTertiary = PressroomColors.LightOnTertiary,
    tertiaryContainer = PressroomColors.LightSageTertiaryContainer,
    onTertiaryContainer = PressroomColors.LightOnTertiaryContainer,
    background = PressroomColors.LightNewsprintBackground,
    onBackground = PressroomColors.LightCocoaText,
    surface = PressroomColors.LightPaperSurface,
    onSurface = PressroomColors.LightCocoaText,
    surfaceVariant = PressroomColors.LightPeachSurfaceVariant,
    onSurfaceVariant = PressroomColors.LightMutedText,
    outline = PressroomColors.LightCaramelOutline,
    outlineVariant = PressroomColors.LightSoftOutlineVariant,
    error = PressroomColors.LightError,
    onError = PressroomColors.LightOnError,
    errorContainer = PressroomColors.LightErrorContainer,
    onErrorContainer = PressroomColors.LightOnErrorContainer,
    surfaceBright = PressroomColors.LightPaperSurface,
    surfaceDim = PressroomColors.LightPeachSurfaceVariant,
    surfaceContainerLowest = PressroomColors.LightPaperSurface,
    surfaceContainerLow = PressroomColors.LightPaperSurface,
    surfaceContainer = PressroomColors.LightPeachSurfaceVariant,
    surfaceContainerHigh = PressroomColors.LightPeachSurfaceVariant,
    surfaceContainerHighest = PressroomColors.LightPeachSurfaceVariant
)

val PressroomDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PressroomColors.DarkPeachPrimary,
    onPrimary = PressroomColors.DarkOnPrimary,
    primaryContainer = PressroomColors.DarkRoastPrimaryContainer,
    onPrimaryContainer = PressroomColors.DarkOnPrimaryContainer,
    secondary = PressroomColors.DarkCaramelSecondary,
    onSecondary = PressroomColors.DarkOnSecondary,
    secondaryContainer = PressroomColors.DarkBarkSecondaryContainer,
    onSecondaryContainer = PressroomColors.DarkOnSecondaryContainer,
    tertiary = PressroomColors.DarkSageTertiary,
    onTertiary = PressroomColors.DarkOnTertiary,
    tertiaryContainer = PressroomColors.DarkMossTertiaryContainer,
    onTertiaryContainer = PressroomColors.DarkOnTertiaryContainer,
    background = PressroomColors.DarkCocoaBackground,
    onBackground = PressroomColors.DarkCreamText,
    surface = PressroomColors.DarkCocoaSurface,
    onSurface = PressroomColors.DarkCreamText,
    surfaceVariant = PressroomColors.DarkCocoaSurfaceVariant,
    onSurfaceVariant = PressroomColors.DarkMutedText,
    outline = PressroomColors.DarkCaramelOutline,
    outlineVariant = PressroomColors.DarkSoftOutlineVariant,
    error = PressroomColors.DarkError,
    onError = PressroomColors.DarkOnError,
    errorContainer = PressroomColors.DarkErrorContainer,
    onErrorContainer = PressroomColors.DarkOnErrorContainer,
    surfaceBright = PressroomColors.DarkCocoaSurfaceVariant,
    surfaceDim = PressroomColors.DarkCocoaSurface,
    surfaceContainerLowest = PressroomColors.DarkCocoaSurface,
    surfaceContainerLow = PressroomColors.DarkCocoaSurface,
    surfaceContainer = PressroomColors.DarkCocoaSurfaceVariant,
    surfaceContainerHigh = PressroomColors.DarkCocoaSurfaceVariant,
    surfaceContainerHighest = PressroomColors.DarkCocoaSurfaceVariant
)

/**
 * Curated color tokens for the Atelier theme ("Tenor Sans Typography" —
 * gallery white, fired terracotta #E67D54 family, warm stone neutrals).
 */
object AtelierColors {
    // Light Palette (white-walled gallery, terracotta accents)
    val LightGalleryBackground = Color(0xFFFDFCFA)
    val LightGallerySurface = Color(0xFFFFFFFF)
    val LightSandSurfaceVariant = Color(0xFFF5EDE4)
    val LightFiredTerracottaPrimary = Color(0xFFB2532B)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightPaleTerracottaContainer = Color(0xFFF7D9C4)
    val LightOnTerracottaContainer = Color(0xFF3A1F12)
    val LightTaupeSecondary = Color(0xFF6B5D4F)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightStoneSecondaryContainer = Color(0xFFE8E0D4)
    val LightOnSecondaryContainer = Color(0xFF2E2620)
    val LightSlateTertiary = Color(0xFF57707A)
    val LightOnTertiary = Color(0xFFFFFFFF)
    val LightMistTertiaryContainer = Color(0xFFD8E4E8)
    val LightOnTertiaryContainer = Color(0xFF1B2E34)
    val LightEspressoText = Color(0xFF2E2620)
    val LightMutedText = Color(0xFF7A6E60)
    val LightClayOutline = Color(0xFFBE7B4E)
    val LightSoftOutlineVariant = Color(0xFFEAD5C0)
    val LightError = Color(0xFFB0362C)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFF6D3CC)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette (terracotta room, cream artwork)
    val DarkTerracottaBackground = Color(0xFFE67D54)
    val DarkEmberSurface = Color(0xFFD96A41)
    val DarkEmberSurfaceVariant = Color(0xFFC85A34)
    val DarkEspressoPrimary = Color(0xFF2E1B12)
    val DarkOnPrimary = Color(0xFFFFF3E8)
    val DarkEspressoPrimaryContainer = Color(0xFF2E1B12)
    val DarkOnEspressoContainer = Color(0xFFF7D9C4)
    val DarkCreamSecondary = Color(0xFFF7E8D6)
    val DarkOnSecondary = Color(0xFF2E1B12)
    val DarkBurntSecondaryContainer = Color(0xFFB9552F)
    val DarkOnSecondaryContainer = Color(0xFFFFF3E8)
    val DarkTealTertiary = Color(0xFF1F3A40)
    val DarkOnTertiary = Color(0xFFD8E4E8)
    val DarkDeepTealContainer = Color(0xFF1F3A40)
    val DarkOnTealContainer = Color(0xFFD8E4E8)
    val DarkCreamText = Color(0xFFFFF3E8)
    val DarkMutedText = Color(0xFFF0CFAE)
    val DarkCreamOutline = Color(0xFFF7DCC2)
    val DarkSoftOutlineVariant = Color(0xFFB9552F)
    val DarkError = Color(0xFF4A0F0A)
    val DarkOnError = Color(0xFFFFD9D2)
    val DarkErrorContainer = Color(0xFF4A0F0A)
    val DarkOnErrorContainer = Color(0xFFFFD9D2)
}

val AtelierLightColorScheme: ColorScheme = lightColorScheme(
    primary = AtelierColors.LightFiredTerracottaPrimary,
    onPrimary = AtelierColors.LightOnPrimary,
    primaryContainer = AtelierColors.LightPaleTerracottaContainer,
    onPrimaryContainer = AtelierColors.LightOnTerracottaContainer,
    secondary = AtelierColors.LightTaupeSecondary,
    onSecondary = AtelierColors.LightOnSecondary,
    secondaryContainer = AtelierColors.LightStoneSecondaryContainer,
    onSecondaryContainer = AtelierColors.LightOnSecondaryContainer,
    tertiary = AtelierColors.LightSlateTertiary,
    onTertiary = AtelierColors.LightOnTertiary,
    tertiaryContainer = AtelierColors.LightMistTertiaryContainer,
    onTertiaryContainer = AtelierColors.LightOnTertiaryContainer,
    background = AtelierColors.LightGalleryBackground,
    onBackground = AtelierColors.LightEspressoText,
    surface = AtelierColors.LightGallerySurface,
    onSurface = AtelierColors.LightEspressoText,
    surfaceVariant = AtelierColors.LightSandSurfaceVariant,
    onSurfaceVariant = AtelierColors.LightMutedText,
    outline = AtelierColors.LightClayOutline,
    outlineVariant = AtelierColors.LightSoftOutlineVariant,
    error = AtelierColors.LightError,
    onError = AtelierColors.LightOnError,
    errorContainer = AtelierColors.LightErrorContainer,
    onErrorContainer = AtelierColors.LightOnErrorContainer,
    surfaceBright = AtelierColors.LightGallerySurface,
    surfaceDim = AtelierColors.LightSandSurfaceVariant,
    surfaceContainerLowest = AtelierColors.LightGallerySurface,
    surfaceContainerLow = AtelierColors.LightGallerySurface,
    surfaceContainer = AtelierColors.LightSandSurfaceVariant,
    surfaceContainerHigh = AtelierColors.LightSandSurfaceVariant,
    surfaceContainerHighest = AtelierColors.LightSandSurfaceVariant
)

val AtelierDarkColorScheme: ColorScheme = darkColorScheme(
    primary = AtelierColors.DarkEspressoPrimary,
    onPrimary = AtelierColors.DarkOnPrimary,
    primaryContainer = AtelierColors.DarkEspressoPrimaryContainer,
    onPrimaryContainer = AtelierColors.DarkOnEspressoContainer,
    secondary = AtelierColors.DarkCreamSecondary,
    onSecondary = AtelierColors.DarkOnSecondary,
    secondaryContainer = AtelierColors.DarkBurntSecondaryContainer,
    onSecondaryContainer = AtelierColors.DarkOnSecondaryContainer,
    tertiary = AtelierColors.DarkTealTertiary,
    onTertiary = AtelierColors.DarkOnTertiary,
    tertiaryContainer = AtelierColors.DarkDeepTealContainer,
    onTertiaryContainer = AtelierColors.DarkOnTealContainer,
    background = AtelierColors.DarkTerracottaBackground,
    onBackground = AtelierColors.DarkCreamText,
    surface = AtelierColors.DarkEmberSurface,
    onSurface = AtelierColors.DarkCreamText,
    surfaceVariant = AtelierColors.DarkEmberSurfaceVariant,
    onSurfaceVariant = AtelierColors.DarkMutedText,
    outline = AtelierColors.DarkCreamOutline,
    outlineVariant = AtelierColors.DarkSoftOutlineVariant,
    error = AtelierColors.DarkError,
    onError = AtelierColors.DarkOnError,
    errorContainer = AtelierColors.DarkErrorContainer,
    onErrorContainer = AtelierColors.DarkOnErrorContainer,
    surfaceBright = AtelierColors.DarkEmberSurface,
    surfaceDim = AtelierColors.DarkEmberSurfaceVariant,
    surfaceContainerLowest = AtelierColors.DarkEmberSurface,
    surfaceContainerLow = AtelierColors.DarkEmberSurface,
    surfaceContainer = AtelierColors.DarkEmberSurfaceVariant,
    surfaceContainerHigh = AtelierColors.DarkEmberSurfaceVariant,
    surfaceContainerHighest = AtelierColors.DarkEmberSurfaceVariant
)

/**
 * Curated color tokens for the Starbase theme ("Space Grotesk Typography" —
 * peach #FFE3C3, oxblood #73001C, console teal accents).
 */
object StarbaseColors {
    // Light Palette (mission control day: warm light, oxblood ink, peach glow)
    val LightFlightBackground = Color(0xFFFAF4EC)
    val LightFlightSurface = Color(0xFFFFFFFF)
    val LightPeachSurfaceVariant = Color(0xFFF3E2CF)
    val LightOxbloodPrimary = Color(0xFF73001C)
    val LightOnPrimary = Color(0xFFFFF7EF)
    val LightPeachPrimaryContainer = Color(0xFFFFE3C3)
    val LightOnPeachContainer = Color(0xFF3A0E14)
    val LightEmberSecondary = Color(0xFFA34A24)
    val LightOnSecondary = Color(0xFFFFF7EF)
    val LightSandSecondaryContainer = Color(0xFFF6D3B8)
    val LightOnSecondaryContainer = Color(0xFF40200F)
    val LightConsoleTertiary = Color(0xFF2E6B62)
    val LightOnTertiary = Color(0xFFFFF7EF)
    val LightMintTertiaryContainer = Color(0xFFCFE4DE)
    val LightOnTertiaryContainer = Color(0xFF0E2E29)
    val LightCockpitText = Color(0xFF2E1B14)
    val LightMutedText = Color(0xFF7A675A)
    val LightCopperOutline = Color(0xFFA9765F)
    val LightSoftOutlineVariant = Color(0xFFE5CDB4)
    val LightError = Color(0xFF9E352C)
    val LightOnError = Color(0xFFFFF7EF)
    val LightErrorContainer = Color(0xFFF5D2CB)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette (night launch: oxblood room, peach instrument glow)
    val DarkOxbloodBackground = Color(0xFF73001C)
    val DarkOxbloodSurface = Color(0xFF7C2132)
    val DarkOxbloodSurfaceVariant = Color(0xFF8E2A3D)
    val DarkPeachPrimary = Color(0xFFFFE3C3)
    val DarkOnPrimary = Color(0xFF3A0E14)
    val DarkWinePrimaryContainer = Color(0xFF9C3550)
    val DarkOnPrimaryContainer = Color(0xFFFFE9D2)
    val DarkAmberSecondary = Color(0xFFE8A06D)
    val DarkOnSecondary = Color(0xFF3A0E14)
    val DarkRustSecondaryContainer = Color(0xFF743A26)
    val DarkOnSecondaryContainer = Color(0xFFF6D3B8)
    val DarkMintTertiary = Color(0xFF7FD1C0)
    val DarkOnTertiary = Color(0xFF0E2E29)
    val DarkPineTertiaryContainer = Color(0xFF1E4A44)
    val DarkOnTertiaryContainer = Color(0xFFCFE4DE)
    val DarkCreamText = Color(0xFFFBEEDF)
    val DarkMutedText = Color(0xFFD8AE93)
    val DarkPeachOutline = Color(0xFFE8B48E)
    val DarkSoftOutlineVariant = Color(0xFF96506A)
    val DarkError = Color(0xFFE89890)
    val DarkOnError = Color(0xFF3A0E0B)
    val DarkErrorContainer = Color(0xFF5C2222)
    val DarkOnErrorContainer = Color(0xFFFFD9D9)
}

val StarbaseLightColorScheme: ColorScheme = lightColorScheme(
    primary = StarbaseColors.LightOxbloodPrimary,
    onPrimary = StarbaseColors.LightOnPrimary,
    primaryContainer = StarbaseColors.LightPeachPrimaryContainer,
    onPrimaryContainer = StarbaseColors.LightOnPeachContainer,
    secondary = StarbaseColors.LightEmberSecondary,
    onSecondary = StarbaseColors.LightOnSecondary,
    secondaryContainer = StarbaseColors.LightSandSecondaryContainer,
    onSecondaryContainer = StarbaseColors.LightOnSecondaryContainer,
    tertiary = StarbaseColors.LightConsoleTertiary,
    onTertiary = StarbaseColors.LightOnTertiary,
    tertiaryContainer = StarbaseColors.LightMintTertiaryContainer,
    onTertiaryContainer = StarbaseColors.LightOnTertiaryContainer,
    background = StarbaseColors.LightFlightBackground,
    onBackground = StarbaseColors.LightCockpitText,
    surface = StarbaseColors.LightFlightSurface,
    onSurface = StarbaseColors.LightCockpitText,
    surfaceVariant = StarbaseColors.LightPeachSurfaceVariant,
    onSurfaceVariant = StarbaseColors.LightMutedText,
    outline = StarbaseColors.LightCopperOutline,
    outlineVariant = StarbaseColors.LightSoftOutlineVariant,
    error = StarbaseColors.LightError,
    onError = StarbaseColors.LightOnError,
    errorContainer = StarbaseColors.LightErrorContainer,
    onErrorContainer = StarbaseColors.LightOnErrorContainer,
    surfaceBright = StarbaseColors.LightFlightSurface,
    surfaceDim = StarbaseColors.LightPeachSurfaceVariant,
    surfaceContainerLowest = StarbaseColors.LightFlightSurface,
    surfaceContainerLow = StarbaseColors.LightFlightSurface,
    surfaceContainer = StarbaseColors.LightPeachSurfaceVariant,
    surfaceContainerHigh = StarbaseColors.LightPeachSurfaceVariant,
    surfaceContainerHighest = StarbaseColors.LightPeachSurfaceVariant
)

val StarbaseDarkColorScheme: ColorScheme = darkColorScheme(
    primary = StarbaseColors.DarkPeachPrimary,
    onPrimary = StarbaseColors.DarkOnPrimary,
    primaryContainer = StarbaseColors.DarkWinePrimaryContainer,
    onPrimaryContainer = StarbaseColors.DarkOnPrimaryContainer,
    secondary = StarbaseColors.DarkAmberSecondary,
    onSecondary = StarbaseColors.DarkOnSecondary,
    secondaryContainer = StarbaseColors.DarkRustSecondaryContainer,
    onSecondaryContainer = StarbaseColors.DarkOnSecondaryContainer,
    tertiary = StarbaseColors.DarkMintTertiary,
    onTertiary = StarbaseColors.DarkOnTertiary,
    tertiaryContainer = StarbaseColors.DarkPineTertiaryContainer,
    onTertiaryContainer = StarbaseColors.DarkOnTertiaryContainer,
    background = StarbaseColors.DarkOxbloodBackground,
    onBackground = StarbaseColors.DarkCreamText,
    surface = StarbaseColors.DarkOxbloodSurface,
    onSurface = StarbaseColors.DarkCreamText,
    surfaceVariant = StarbaseColors.DarkOxbloodSurfaceVariant,
    onSurfaceVariant = StarbaseColors.DarkMutedText,
    outline = StarbaseColors.DarkPeachOutline,
    outlineVariant = StarbaseColors.DarkSoftOutlineVariant,
    error = StarbaseColors.DarkError,
    onError = StarbaseColors.DarkOnError,
    errorContainer = StarbaseColors.DarkErrorContainer,
    onErrorContainer = StarbaseColors.DarkOnErrorContainer,
    surfaceBright = StarbaseColors.DarkOxbloodSurfaceVariant,
    surfaceDim = StarbaseColors.DarkOxbloodSurface,
    surfaceContainerLowest = StarbaseColors.DarkOxbloodSurface,
    surfaceContainerLow = StarbaseColors.DarkOxbloodSurface,
    surfaceContainer = StarbaseColors.DarkOxbloodSurfaceVariant,
    surfaceContainerHigh = StarbaseColors.DarkOxbloodSurfaceVariant,
    surfaceContainerHighest = StarbaseColors.DarkOxbloodSurfaceVariant
)

/**
 * Curated color tokens for the Cottage theme ("Shabby Chic" — faded rose
 * #E8C4C4, antique white, soft sage, dusty blue, lavender mist).
 */
object CottageColors {
    // Light Palette (sun-bleached cottage: antique white, rosewood ink, sage)
    val LightAntiqueBackground = Color(0xFFFAF0E6)
    val LightCottageSurface = Color(0xFFFFFDF9)
    val LightLinenSurfaceVariant = Color(0xFFF0E6D6)
    val LightRosewoodPrimary = Color(0xFFA85D68)
    val LightOnPrimary = Color(0xFFFFFDF9)
    val LightFadedRoseContainer = Color(0xFFE8C4C4)
    val LightOnRoseContainer = Color(0xFF4A2429)
    val LightSageSecondary = Color(0xFF5F7D5F)
    val LightOnSecondary = Color(0xFFFFFDF9)
    val LightMistSecondaryContainer = Color(0xFFC5D5C5)
    val LightOnSecondaryContainer = Color(0xFF23331F)
    val LightDustyBlueTertiary = Color(0xFF527791)
    val LightOnTertiary = Color(0xFFFFFDF9)
    val LightPowderTertiaryContainer = Color(0xFFC9D8E2)
    val LightOnTertiaryContainer = Color(0xFF22333D)
    val LightUmberText = Color(0xFF463C36)
    val LightMutedText = Color(0xFF8A7A6E)
    val LightRoseTaupeOutline = Color(0xFFC4A69B)
    val LightSoftOutlineVariant = Color(0xFFE5D5CA)
    val LightError = Color(0xFFA65044)
    val LightOnError = Color(0xFFFFFDF9)
    val LightErrorContainer = Color(0xFFF5D5CB)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette (candlelit dusk: cocoa-plum room, faded rose glow)
    val DarkPlumBackground = Color(0xFF2E2426)
    val DarkPlumSurface = Color(0xFF3A2E31)
    val DarkPlumSurfaceVariant = Color(0xFF4A3A3E)
    val DarkFadedRosePrimary = Color(0xFFE8C4C4)
    val DarkOnPrimary = Color(0xFF3A2226)
    val DarkMauvePrimaryContainer = Color(0xFF6E4A52)
    val DarkOnPrimaryContainer = Color(0xFFF5D5D5)
    val DarkSageSecondary = Color(0xFFA9C2A9)
    val DarkOnSecondary = Color(0xFF23331F)
    val DarkMossSecondaryContainer = Color(0xFF3E523E)
    val DarkOnSecondaryContainer = Color(0xFFD3E2D3)
    val DarkDustyBlueTertiary = Color(0xFFA4B8C4)
    val DarkOnTertiary = Color(0xFF22333D)
    val DarkSlateTertiaryContainer = Color(0xFF3A4C59)
    val DarkOnTertiaryContainer = Color(0xFFD3E0E8)
    val DarkCreamText = Color(0xFFF5E9E2)
    val DarkMutedText = Color(0xFFC4AEA3)
    val DarkRoseGlowOutline = Color(0xFFC99B90)
    val DarkSoftOutlineVariant = Color(0xFF544246)
    val DarkError = Color(0xFFE89890)
    val DarkOnError = Color(0xFF3A0E0B)
    val DarkErrorContainer = Color(0xFF5C2222)
    val DarkOnErrorContainer = Color(0xFFFFD9D9)
}

val CottageLightColorScheme: ColorScheme = lightColorScheme(
    primary = CottageColors.LightRosewoodPrimary,
    onPrimary = CottageColors.LightOnPrimary,
    primaryContainer = CottageColors.LightFadedRoseContainer,
    onPrimaryContainer = CottageColors.LightOnRoseContainer,
    secondary = CottageColors.LightSageSecondary,
    onSecondary = CottageColors.LightOnSecondary,
    secondaryContainer = CottageColors.LightMistSecondaryContainer,
    onSecondaryContainer = CottageColors.LightOnSecondaryContainer,
    tertiary = CottageColors.LightDustyBlueTertiary,
    onTertiary = CottageColors.LightOnTertiary,
    tertiaryContainer = CottageColors.LightPowderTertiaryContainer,
    onTertiaryContainer = CottageColors.LightOnTertiaryContainer,
    background = CottageColors.LightAntiqueBackground,
    onBackground = CottageColors.LightUmberText,
    surface = CottageColors.LightCottageSurface,
    onSurface = CottageColors.LightUmberText,
    surfaceVariant = CottageColors.LightLinenSurfaceVariant,
    onSurfaceVariant = CottageColors.LightMutedText,
    outline = CottageColors.LightRoseTaupeOutline,
    outlineVariant = CottageColors.LightSoftOutlineVariant,
    error = CottageColors.LightError,
    onError = CottageColors.LightOnError,
    errorContainer = CottageColors.LightErrorContainer,
    onErrorContainer = CottageColors.LightOnErrorContainer,
    surfaceBright = CottageColors.LightCottageSurface,
    surfaceDim = CottageColors.LightLinenSurfaceVariant,
    surfaceContainerLowest = CottageColors.LightCottageSurface,
    surfaceContainerLow = CottageColors.LightCottageSurface,
    surfaceContainer = CottageColors.LightLinenSurfaceVariant,
    surfaceContainerHigh = CottageColors.LightLinenSurfaceVariant,
    surfaceContainerHighest = CottageColors.LightLinenSurfaceVariant
)

val CottageDarkColorScheme: ColorScheme = darkColorScheme(
    primary = CottageColors.DarkFadedRosePrimary,
    onPrimary = CottageColors.DarkOnPrimary,
    primaryContainer = CottageColors.DarkMauvePrimaryContainer,
    onPrimaryContainer = CottageColors.DarkOnPrimaryContainer,
    secondary = CottageColors.DarkSageSecondary,
    onSecondary = CottageColors.DarkOnSecondary,
    secondaryContainer = CottageColors.DarkMossSecondaryContainer,
    onSecondaryContainer = CottageColors.DarkOnSecondaryContainer,
    tertiary = CottageColors.DarkDustyBlueTertiary,
    onTertiary = CottageColors.DarkOnTertiary,
    tertiaryContainer = CottageColors.DarkSlateTertiaryContainer,
    onTertiaryContainer = CottageColors.DarkOnTertiaryContainer,
    background = CottageColors.DarkPlumBackground,
    onBackground = CottageColors.DarkCreamText,
    surface = CottageColors.DarkPlumSurface,
    onSurface = CottageColors.DarkCreamText,
    surfaceVariant = CottageColors.DarkPlumSurfaceVariant,
    onSurfaceVariant = CottageColors.DarkMutedText,
    outline = CottageColors.DarkRoseGlowOutline,
    outlineVariant = CottageColors.DarkSoftOutlineVariant,
    error = CottageColors.DarkError,
    onError = CottageColors.DarkOnError,
    errorContainer = CottageColors.DarkErrorContainer,
    onErrorContainer = CottageColors.DarkOnErrorContainer,
    surfaceBright = CottageColors.DarkPlumSurfaceVariant,
    surfaceDim = CottageColors.DarkPlumSurface,
    surfaceContainerLowest = CottageColors.DarkPlumSurface,
    surfaceContainerLow = CottageColors.DarkPlumSurface,
    surfaceContainer = CottageColors.DarkPlumSurfaceVariant,
    surfaceContainerHigh = CottageColors.DarkPlumSurfaceVariant,
    surfaceContainerHighest = CottageColors.DarkPlumSurfaceVariant
)
