package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayLayoutPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayLayoutPolicyTest {

    @Test
    fun smallViewportWinsOverNominalPanelMinimums() {
        val bounds = OverlayLayoutPolicy.clampPanelBounds(
            x = 800, y = 900, width = 700, height = 800,
            screenWidth = 180, screenHeight = 220,
            minWidth = 300, minHeight = 672, maxWidth = 169, maxHeight = 187
        )
        assertTrue(bounds.width in 1..180)
        assertTrue(bounds.height in 1..220)
        assertTrue(bounds.x + bounds.width <= 180)
        assertTrue(bounds.y + bounds.height <= 220)
    }

    @Test
    fun oversizedBubbleStillFitsSmallViewport() {
        val bounds = OverlayLayoutPolicy.clampBubbleBounds(50, 50, 100, 40, 30)
        assertEquals(30, bounds.size)
        assertTrue(bounds.x + bounds.size <= 40)
        assertTrue(bounds.y + bounds.size <= 30)
    }

    @Test
    fun testGridCellSizeNeverExceedsPanelWidthAcrossDimensions() {
        val density = 2.5f
        val horizontalPadding = (6 * density).toInt()
        val cellMargin = (3 * density).toInt()

        val testWidths = listOf(240, 300, 360, 480, 720, 1080, 1440)
        for (width in testWidths) {
            val cellSize = OverlayLayoutPolicy.gridCellSize(
                panelWidthPx = width,
                horizontalContentPaddingPx = horizontalPadding,
                cellMarginPx = cellMargin
            )

            val totalUsedWidth = (OverlayLayoutPolicy.GRID_COLUMNS * cellSize) +
                    (OverlayLayoutPolicy.GRID_COLUMNS * 2 * cellMargin) +
                    (2 * horizontalPadding)

            assertTrue(
                "Total used width ($totalUsedWidth) must be <= panelWidth ($width)",
                totalUsedWidth <= width
            )
            assertTrue("Cell size must be positive", cellSize > 0)
        }
    }

    @Test
    fun testGridOnlyMinHeightIsSmallerThanChromeFullAndHoldsAtLeastTwoRows() {
        val density = 2.0f

        val gridOnlyMinHeight = OverlayLayoutPolicy.minPanelHeightPx(
            density = density,
            showTitle = false,
            showSearch = false,
            showCategories = false
        )

        val fullChromeMinHeight = OverlayLayoutPolicy.minPanelHeightPx(
            density = density,
            showTitle = true,
            showSearch = true,
            showCategories = true
        )

        assertTrue(
            "Grid-only height ($gridOnlyMinHeight) must be strictly smaller than full chrome ($fullChromeMinHeight)",
            gridOnlyMinHeight < fullChromeMinHeight
        )

        // Verify grid-only contains at least two rows of min-sized stickers
        val twoRowsStickersMinPx = (2 * OverlayLayoutPolicy.MIN_CELL_SIZE_DP * density).toInt()
        assertTrue(
            "Grid-only min height must accommodate at least 2 rows of stickers",
            gridOnlyMinHeight >= twoRowsStickersMinPx
        )
    }

    @Test
    fun testChromeHiddenLeavesZeroChromeOverhead() {
        val density = 3.0f
        val zeroChrome = OverlayLayoutPolicy.chromeHeightPx(
            density = density,
            showTitle = false,
            showSearch = false,
            showCategories = false
        )
        assertEquals("When chrome is disabled, chrome height must be exactly 0", 0, zeroChrome)
    }

    @Test
    fun testPanelBoundsClampingGuaranteesWithinViewport() {
        val screenWidth = 1080
        val screenHeight = 2400

        // Test out-of-bounds positions and dimensions
        data class TestCase(val x: Int, val y: Int, val w: Int, val h: Int)
        val testCases = listOf(
            TestCase(-100, -200, 2000, 3000), // Exceeds all boundaries
            TestCase(1500, 2600, 100, 100),   // Far outside bottom-right
            TestCase(500, 800, 400, 500)       // Normal within bounds
        )

        for ((x, y, w, h) in testCases) {
            val bounds = OverlayLayoutPolicy.clampPanelBounds(
                x = x,
                y = y,
                width = w,
                height = h,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                minWidth = 300,
                minHeight = 300,
                maxWidth = 900,
                maxHeight = 1600
            )

            assertTrue("Width must be >= minWidth", bounds.width >= 300)
            assertTrue("Width must be <= maxWidth", bounds.width <= 900)
            assertTrue("Height must be >= minHeight", bounds.height >= 300)
            assertTrue("Height must be <= maxHeight", bounds.height <= 1600)

            assertTrue("X must be >= 0", bounds.x >= 0)
            assertTrue("Right edge must not exceed screen width", bounds.x + bounds.width <= screenWidth)

            assertTrue("Y must be >= 0", bounds.y >= 0)
            assertTrue("Bottom edge must not exceed screen height", bounds.y + bounds.height <= screenHeight)
        }
    }

    @Test
    fun testBubbleSizeClampingEnforces32To72Range() {
        assertEquals(32f, OverlayLayoutPolicy.clampBubbleSize(20f), 0.001f)
        assertEquals(32f, OverlayLayoutPolicy.clampBubbleSize(32f), 0.001f)
        assertEquals(40f, OverlayLayoutPolicy.clampBubbleSize(40f), 0.001f)
        assertEquals(56f, OverlayLayoutPolicy.clampBubbleSize(56f), 0.001f)
        assertEquals(72f, OverlayLayoutPolicy.clampBubbleSize(72f), 0.001f)
        assertEquals(72f, OverlayLayoutPolicy.clampBubbleSize(100f), 0.001f)
    }

    @Test
    fun testAllEightChromeVisibilityCombinationsProduceValidHeights() {
        val density = 2.625f
        val booleans = listOf(false, true)

        var testedCombinations = 0
        for (title in booleans) {
            for (search in booleans) {
                for (cat in booleans) {
                    val minHeight = OverlayLayoutPolicy.minPanelHeightPx(
                        density = density,
                        showTitle = title,
                        showSearch = search,
                        showCategories = cat
                    )
                    val chromeH = OverlayLayoutPolicy.chromeHeightPx(
                        density = density,
                        showTitle = title,
                        showSearch = search,
                        showCategories = cat
                    )

                    assertTrue("Min height must be positive for all combinations", minHeight > 0)
                    assertTrue("Chrome height must be non-negative", chromeH >= 0)
                    testedCombinations++
                }
            }
        }
        assertEquals("Must test all 8 chrome combinations", 8, testedCombinations)
    }

    @Test
    fun testNormalizeAndDenormalizePositionAcrossRotations() {
        // Portrait bounds
        val portraitW = 1080
        val portraitH = 2400
        val bubbleSize = 120
        val portraitMaxX = portraitW - bubbleSize // 960
        val portraitMaxY = portraitH - bubbleSize // 2280

        // Place near right edge, upper third
        val initialX = 900
        val initialY = 760

        val normalized = OverlayLayoutPolicy.normalizePosition(
            x = initialX,
            y = initialY,
            maxX = portraitMaxX,
            maxY = portraitMaxY
        )
        assertTrue("fractionX must be between 0 and 1", normalized.fractionX in 0f..1f)
        assertTrue("fractionY must be between 0 and 1", normalized.fractionY in 0f..1f)
        assertEquals(900f / 960f, normalized.fractionX, 0.001f)
        assertEquals(760f / 2280f, normalized.fractionY, 0.001f)

        // Rotate to landscape: 2400 x 1080
        val landscapeW = 2400
        val landscapeH = 1080
        val landscapeMaxX = landscapeW - bubbleSize // 2280
        val landscapeMaxY = landscapeH - bubbleSize // 960

        val (landX, landY) = OverlayLayoutPolicy.denormalizePosition(
            normalized = normalized,
            maxX = landscapeMaxX,
            maxY = landscapeMaxY
        )
        assertTrue("Landscape X must be within 0..landscapeMaxX", landX in 0..landscapeMaxX)
        assertTrue("Landscape Y must be within 0..landscapeMaxY", landY in 0..landscapeMaxY)
        // Land X should stay near right edge (~ 2138px)
        assertTrue("Landscape X must stay near right edge", landX > 2000)

        // Rotate back to portrait
        val (restoredX, restoredY) = OverlayLayoutPolicy.denormalizePosition(
            normalized = normalized,
            maxX = portraitMaxX,
            maxY = portraitMaxY
        )
        assertEquals(initialX, restoredX)
        assertEquals(initialY, restoredY)
    }

    @Test
    fun testNormalizeWithZeroOrNegativeMaxBoundsDoesNotCrashAndReturnsZero() {
        val normalized = OverlayLayoutPolicy.normalizePosition(x = 100, y = 200, maxX = 0, maxY = 0)
        assertEquals(0f, normalized.fractionX, 0.0001f)
        assertEquals(0f, normalized.fractionY, 0.0001f)

        val (pxX, pxY) = OverlayLayoutPolicy.denormalizePosition(normalized, maxX = 0, maxY = 0)
        assertEquals(0, pxX)
        assertEquals(0, pxY)

        val negativeNorm = OverlayLayoutPolicy.normalizePosition(x = 50, y = 50, maxX = -10, maxY = -20)
        assertEquals(0f, negativeNorm.fractionX, 0.0001f)
        assertEquals(0f, negativeNorm.fractionY, 0.0001f)
    }

    @Test
    fun testClampBubbleBoundsEnforcesScreenLimits() {
        val screenW = 1080
        val screenH = 2400
        val bubbleSize = 100

        // Negative coordinates
        val underflow = OverlayLayoutPolicy.clampBubbleBounds(-50, -30, bubbleSize, screenW, screenH)
        assertEquals(0, underflow.x)
        assertEquals(0, underflow.y)
        assertEquals(screenW - bubbleSize, underflow.maxX)
        assertEquals(screenH - bubbleSize, underflow.maxY)

        // Overflow coordinates
        val overflow = OverlayLayoutPolicy.clampBubbleBounds(2000, 3000, bubbleSize, screenW, screenH)
        assertEquals(screenW - bubbleSize, overflow.x)
        assertEquals(screenH - bubbleSize, overflow.y)

        // In-bounds coordinates
        val inBounds = OverlayLayoutPolicy.clampBubbleBounds(500, 1000, bubbleSize, screenW, screenH)
        assertEquals(500, inBounds.x)
        assertEquals(1000, inBounds.y)
    }

    @Test
    fun testCalculateColumnsScalesDynamically() {
        val density = 2.0f
        // 240dp -> 3 cols min
        assertEquals(3, OverlayLayoutPolicy.calculateColumns((240 * density).toInt(), density))
        // 320dp -> 3 cols
        assertEquals(3, OverlayLayoutPolicy.calculateColumns((320 * density).toInt(), density))
        // 420dp -> 4 or 5 cols
        val cols420 = OverlayLayoutPolicy.calculateColumns((420 * density).toInt(), density)
        assertTrue(cols420 in 4..5)
        // 600dp -> 6 or 7 cols
        val cols600 = OverlayLayoutPolicy.calculateColumns((600 * density).toInt(), density)
        assertTrue(cols600 >= 6)
    }

    @Test
    fun testComputePopupGeometryReturnsConsistentSnapshot() {
        val density = 2.5f
        val geometry = OverlayLayoutPolicy.computePopupGeometry(
            requestedX = 100,
            requestedY = 200,
            requestedWidth = 800,
            requestedHeight = 1000,
            screenWidth = 1080,
            screenHeight = 2400,
            density = density,
            showTitle = true,
            showSearch = true,
            showCategories = true,
            closeButtonSizePx = (42 * density).toInt(),
            closeOffsetPx = (4 * density).toInt()
        )

        assertTrue(geometry.panelBounds.width in geometry.minWidth..geometry.maxWidth)
        assertTrue(geometry.panelBounds.height in geometry.minHeight..geometry.maxHeight)
        assertTrue(geometry.panelBounds.x + geometry.panelBounds.width <= 1080)
        assertTrue(geometry.panelBounds.y + geometry.panelBounds.height <= 2400)
        assertTrue(geometry.columns >= 3)
        // Close button center should dock on the outside corner
        val closeSize = (42 * density).toInt()
        val closeCenterX = geometry.closePosition.x + closeSize / 2
        val closeCenterY = geometry.closePosition.y + closeSize / 2
        assertTrue(closeCenterX >= geometry.panelBounds.x + geometry.panelBounds.width)
        assertTrue(closeCenterY <= geometry.panelBounds.y)
    }
}
