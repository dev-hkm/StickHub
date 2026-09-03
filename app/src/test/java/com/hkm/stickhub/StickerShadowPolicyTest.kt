package com.hkm.stickhub

import com.hkm.stickhub.service.StickerShadowPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StickerShadowPolicyTest {

    @Test
    fun testZeroStrengthDisablesShadow() {
        assertFalse(StickerShadowPolicy.isShadowEnabled(0.0f))
        assertFalse(StickerShadowPolicy.isShadowEnabled(0.005f))
        assertTrue(StickerShadowPolicy.isShadowEnabled(0.02f))
        assertTrue(StickerShadowPolicy.isShadowEnabled(0.45f))
        assertTrue(StickerShadowPolicy.isShadowEnabled(1.0f))
    }

    @Test
    fun testShadowStrengthMapsToValidBlurAndAlpha() {
        val density = 2.0f
        val minBlur = StickerShadowPolicy.resolveBlurRadius(0.0f, density)
        val midBlur = StickerShadowPolicy.resolveBlurRadius(0.5f, density)
        val maxBlur = StickerShadowPolicy.resolveBlurRadius(1.0f, density)

        assertTrue(minBlur >= 1f)
        assertTrue(midBlur > minBlur)
        assertTrue(maxBlur > midBlur)
        assertTrue(maxBlur <= 30f)

        val minAlpha = StickerShadowPolicy.resolveShadowAlpha(0.0f)
        val midAlpha = StickerShadowPolicy.resolveShadowAlpha(0.5f)
        val maxAlpha = StickerShadowPolicy.resolveShadowAlpha(1.0f)

        assertEquals(0, minAlpha)
        assertEquals(80, midAlpha)
        assertEquals(160, maxAlpha)
        assertTrue(maxAlpha <= 255)
    }

    @Test
    fun testShadowColorIsNotPureBlack() {
        val lightShadow = StickerShadowPolicy.resolveShadowColor(0.5f, isDark = false)
        val darkShadow = StickerShadowPolicy.resolveShadowColor(0.5f, isDark = true)

        // Pure black with full alpha is 0xFF000000. Our shadow has subtle tone and gentle alpha
        assertNotEquals(0xFF000000.toInt(), lightShadow)
        assertNotEquals(0xFF000000.toInt(), darkShadow)

        // RGB channels have subtle charcoal / deep navy undertones
        val r = (lightShadow shr 16) and 0xFF
        val g = (lightShadow shr 8) and 0xFF
        val b = lightShadow and 0xFF
        assertTrue(r > 0 || g > 0 || b > 0)
    }

    @Test
    fun testCacheKeyChangesWhenParametersChange() {
        val keyBase = StickerShadowPolicy.buildCacheKey(
            filePath = "/data/user/0/stickers/cat.png",
            lastModified = 1000L,
            targetSize = 120,
            shadowStrength = 0.45f,
            isDark = false
        )

        val keyFileChanged = StickerShadowPolicy.buildCacheKey(
            filePath = "/data/user/0/stickers/dog.png",
            lastModified = 1000L,
            targetSize = 120,
            shadowStrength = 0.45f,
            isDark = false
        )
        assertNotEquals(keyBase, keyFileChanged)

        val keySizeChanged = StickerShadowPolicy.buildCacheKey(
            filePath = "/data/user/0/stickers/cat.png",
            lastModified = 1000L,
            targetSize = 160,
            shadowStrength = 0.45f,
            isDark = false
        )
        assertNotEquals(keyBase, keySizeChanged)

        val keyStrengthChanged = StickerShadowPolicy.buildCacheKey(
            filePath = "/data/user/0/stickers/cat.png",
            lastModified = 1000L,
            targetSize = 120,
            shadowStrength = 0.85f,
            isDark = false
        )
        assertNotEquals(keyBase, keyStrengthChanged)

        val keyThemeChanged = StickerShadowPolicy.buildCacheKey(
            filePath = "/data/user/0/stickers/cat.png",
            lastModified = 1000L,
            targetSize = 120,
            shadowStrength = 0.45f,
            isDark = true
        )
        assertNotEquals(keyBase, keyThemeChanged)
    }
}
