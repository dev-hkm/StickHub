package com.hkm.stickhub.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

/**
 * Centralized motion tokens for StickHub.
 *
 * Implements Material 3 motion principles:
 * - Deliberate, natural transitions (160ms - 420ms) without delaying direct manipulation
 * - Subtle spatial shifts instead of large jumps
 * - Decelerated arrival and accelerated exit without bounce
 */
object StickHubMotion {

    const val DurationMicro = 160
    const val DurationShort = 240
    const val DurationMedium = 320
    const val DurationLong = 420

    const val CardPressScale = 0.975f
    const val CandidatePressScale = 0.978f

    val EasingStandard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    fun <T> subtleSpring(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = Spring.StiffnessMedium,
        visibilityThreshold: T? = null
    ) = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
        visibilityThreshold = visibilityThreshold
    )

    val TopBarTransform: ContentTransform =
        (slideInVertically(
            animationSpec = tween(DurationShort, easing = EasingEmphasizedDecelerate),
            initialOffsetY = { -it / 4 }
        ) + fadeIn(
            animationSpec = tween(DurationShort)
        )).togetherWith(
            slideOutVertically(
                animationSpec = tween(DurationShort, easing = EasingEmphasizedAccelerate),
                targetOffsetY = { -it / 4 }
            ) + fadeOut(
                animationSpec = tween(DurationShort)
            )
        )

    val BannerEnter = slideInVertically(
        animationSpec = tween(DurationShort, easing = EasingEmphasizedDecelerate),
        initialOffsetY = { -it / 5 }
    ) + fadeIn(animationSpec = tween(DurationShort))

    val BannerExit = slideOutVertically(
        animationSpec = tween(DurationMicro, easing = EasingEmphasizedAccelerate),
        targetOffsetY = { -it / 5 }
    ) + fadeOut(animationSpec = tween(DurationMicro))

    val ItemPlacementSpec = tween<IntOffset>(
        durationMillis = DurationMedium,
        easing = EasingEmphasizedDecelerate
    )
}
