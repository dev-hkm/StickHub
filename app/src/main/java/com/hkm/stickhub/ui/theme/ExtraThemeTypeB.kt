package com.hkm.stickhub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hkm.stickhub.R

// ------------------------------------------------------- Batch-B families ---
val VarelaFontFamily = FontFamily(
    Font(R.font.varela_round_regular, FontWeight.Normal)
)

val NunitoFontFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

val SpecialEliteFontFamily = FontFamily(
    Font(R.font.special_elite_regular, FontWeight.Normal)
)

val QuattrocentoFontFamily = FontFamily(
    Font(R.font.quattrocento_regular, FontWeight.Normal),
    Font(R.font.quattrocento_bold, FontWeight.Bold)
)

// ------------------------------------------------------ Kawaii type -------
// Varela Round ships a single cut: presence comes from roundness + spacing.
val KawaiiTypography = Typography(
    displayLarge = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 52.sp, lineHeight = 58.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 42.sp, lineHeight = 48.sp, letterSpacing = (-0.25).sp),
    displaySmall = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = 0.1.sp),
    headlineSmall = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.15.sp),
    titleLarge = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.15.sp),
    titleMedium = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodyLarge = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = VarelaFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp)
)

// ---------------------------------------------------- Solarpunk type -------
val SolarpunkTypography = Typography(
    displayLarge = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 54.sp, lineHeight = 58.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 43.sp, lineHeight = 47.sp, letterSpacing = (-0.75).sp),
    displaySmall = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 39.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 35.sp, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 31.sp),
    headlineSmall = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

// -------------------------------------------------------- Noir type -------
// Special Elite ships a single typewriter cut: voice comes from texture.
val NoirTypography = Typography(
    displayLarge = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 50.sp, lineHeight = 56.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 40.sp, lineHeight = 46.sp),
    displaySmall = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 38.sp),
    headlineLarge = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.1.sp),
    headlineSmall = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.15.sp),
    titleLarge = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleMedium = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp, letterSpacing = 0.25.sp),
    bodyLarge = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 21.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.25.sp),
    labelLarge = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = SpecialEliteFontFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp)
)

// ------------------------------------------------------- Glass type -------
val GlassTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Light, fontSize = 57.sp, lineHeight = 62.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Light, fontSize = 45.sp, lineHeight = 50.sp, letterSpacing = (-0.75).sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.1.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.1.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.3.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.3.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.6.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.sp)
)

// ----------------------------------------------------- Nouveau type -------
val NouveauTypography = Typography(
    displayLarge = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 54.sp, lineHeight = 60.sp, letterSpacing = (-0.75).sp),
    displayMedium = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 43.sp, lineHeight = 49.sp, letterSpacing = (-0.5).sp),
    displaySmall = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.25).sp),
    headlineLarge = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = 0.1.sp),
    headlineSmall = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.15.sp),
    titleLarge = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.15.sp),
    titleMedium = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodyLarge = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = QuattrocentoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp)
)
