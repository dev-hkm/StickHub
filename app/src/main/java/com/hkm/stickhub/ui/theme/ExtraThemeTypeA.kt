package com.hkm.stickhub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hkm.stickhub.R

// ------------------------------------------------------- Batch-A families ---
val OutfitFontFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

val SpaceMonoFontFamily = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold)
)

val CinzelFontFamily = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold),
    Font(R.font.cinzel_black, FontWeight.Black)
)

val PressStartFontFamily = FontFamily(
    Font(R.font.press_start_regular, FontWeight.Normal)
)

// ------------------------------------------------------- Aurora type -------
val AuroraTypography = Typography(
    displayLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 54.sp, lineHeight = 60.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 43.sp, lineHeight = 48.sp, letterSpacing = (-0.75).sp),
    displaySmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.25).sp),
    headlineSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp)
)

// ---------------------------------------------------- Synthwave type -------
val SynthwaveTypography = Typography(
    displayLarge = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 50.sp, lineHeight = 56.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.75).sp),
    displaySmall = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 31.sp),
    headlineSmall = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    bodyMedium = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 21.sp, letterSpacing = 0.15.sp),
    bodySmall = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
    labelLarge = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = SpaceMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp)
)

// ------------------------------------------------------ Gatsby type -------
val GatsbyTypography = Typography(
    displayLarge = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-0.5).sp),
    displaySmall = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 35.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 31.sp, lineHeight = 37.sp),
    headlineMedium = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 33.sp),
    headlineSmall = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = 0.1.sp),
    titleLarge = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = 0.1.sp),
    titleMedium = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp),
    bodyLarge = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.7.sp),
    labelSmall = TextStyle(fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.9.sp)
)

// ------------------------------------------------------- Ukiyo type -------
val UkiyoTypography = Typography(
    displayLarge = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Black, fontSize = 52.sp, lineHeight = 58.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Black, fontSize = 41.sp, lineHeight = 47.sp, letterSpacing = (-0.25).sp),
    displaySmall = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 33.sp, lineHeight = 39.sp),
    headlineLarge = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 35.sp),
    headlineMedium = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 31.sp, letterSpacing = 0.1.sp),
    headlineSmall = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = 0.15.sp),
    titleLarge = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = 0.15.sp),
    titleMedium = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodyLarge = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 21.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.7.sp),
    labelSmall = TextStyle(fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.9.sp)
)

// ------------------------------------------------------- Pixel type -------
// Press Start 2P is oversized by design: the whole scale runs ~60% with
// generous leading so UI text never overflows.
val PixelTypography = Typography(
    displayLarge = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 44.sp),
    displayMedium = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 36.sp),
    displaySmall = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 21.sp, lineHeight = 30.sp),
    headlineLarge = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    headlineMedium = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    headlineSmall = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    titleLarge = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    titleMedium = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 19.sp),
    titleSmall = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 19.sp),
    bodyMedium = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 16.sp),
    labelMedium = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, lineHeight = 15.sp),
    labelSmall = TextStyle(fontFamily = PressStartFontFamily, fontWeight = FontWeight.Normal, fontSize = 8.sp, lineHeight = 14.sp)
)
