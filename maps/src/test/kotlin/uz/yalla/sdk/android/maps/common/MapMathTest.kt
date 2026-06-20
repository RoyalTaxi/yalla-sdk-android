package uz.yalla.sdk.android.maps.common

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import uz.yalla.maps.api.model.Anchor
import uz.yalla.maps.api.model.MapMarkerIcon
import uz.yalla.maps.api.model.RoutePattern

/**
 * Characterization tests for [MapMath] — the framework-free boundary math the Android renderers
 * depend on (anchor classification, the circle-radius zoom curve, dash arrays, the fit-margin clamp,
 * icon-cache identity, and the settled-pose epsilon that drives the shared [MarkerMotionDriver]).
 * These pin buyer-visible rendering correctness that was previously unpinned (review finding #3) and
 * guard the divergence-from-iOS fixes (#1 settled skip, #6/#8 parity) from silent regression.
 */
class MapMathTest {

    // --- toLibreAnchor (review #3: hand-rolled classifier with magic 0.9/0.1/0.4..0.6 bands) ---

    @Test
    fun bottomAnchorMapsToBottom() {
        assertEquals("bottom", MapMath.toLibreAnchor(Anchor.BOTTOM))
    }

    @Test
    fun topAnchorMapsToTop() {
        assertEquals("top", MapMath.toLibreAnchor(Anchor.TOP))
    }

    @Test
    fun centerAnchorFallsThroughToCenter() {
        assertEquals("center", MapMath.toLibreAnchor(Anchor.CENTER))
    }

    @Test
    fun leftEdgeAnchorMapsToLeft() {
        assertEquals("left", MapMath.toLibreAnchor(Anchor(0.0f, 0.5f)))
    }

    @Test
    fun rightEdgeAnchorMapsToRight() {
        assertEquals("right", MapMath.toLibreAnchor(Anchor(1.0f, 0.5f)))
    }

    @Test
    fun offGridAnchorRoundsToCenter() {
        assertEquals("center", MapMath.toLibreAnchor(Anchor(0.3f, 0.3f)))
    }

    // --- circleRadiusPixelsAtZoomZero (review #3: meters-per-pixel x cos(lat) curve, iOS parity) ---

    @Test
    fun circleRadiusAtEquatorMatchesGroundResolution() {
        // At the equator cos(0) = 1, so the pixel radius is radiusMeters / BASE_METERS_PER_PIXEL.
        val expected = (50.0 / MapMath.BASE_METERS_PER_PIXEL).toFloat()
        assertEquals(expected, MapMath.circleRadiusPixelsAtZoomZero(50.0, 0.0), 1e-9f)
    }

    @Test
    fun circleRadiusGrowsWithLatitudeAsMetersPerPixelShrinks() {
        // Mercator: metres-per-pixel shrinks toward the poles (divides by cos(lat)), so the same
        // ground radius is more pixels at higher latitude.
        val atEquator = MapMath.circleRadiusPixelsAtZoomZero(50.0, 0.0)
        val atSixty = MapMath.circleRadiusPixelsAtZoomZero(50.0, 60.0)
        // cos(60) = 0.5, so the radius roughly doubles.
        assertTrue(atSixty > atEquator)
        assertTrue(abs(atSixty - atEquator * 2f) < atEquator * 0.01f)
    }

    @Test
    fun circleRadiusScaleMatchesIosTwoToThe22() {
        // iOS multiplies the zoom-0 radius by 4194304.0 (= 2^22) at zoom 22; parity must hold.
        assertEquals(4_194_304f, MapMath.CIRCLE_RADIUS_SCALE)
        assertEquals(22, MapMath.CIRCLE_RADIUS_MAX_ZOOM)
    }

    // --- toLibreDashArray (review #3: divides by widthDp; widthDp == 0 -> Infinity) ---

    @Test
    fun solidPatternHasNoDashArray() {
        assertNull(MapMath.toLibreDashArray(RoutePattern.SOLID, 4f))
    }

    @Test
    fun dashedPatternScalesByWidth() {
        val dash = MapMath.toLibreDashArray(RoutePattern.DASHED, 4f)!!
        assertEquals(2, dash.size)
        assertEquals(30f / 4f, dash[0])
        assertEquals(20f / 4f, dash[1])
    }

    @Test
    fun dottedPatternStartsWithZero() {
        val dash = MapMath.toLibreDashArray(RoutePattern.DOTTED, 4f)!!
        assertEquals(0f, dash[0])
        assertEquals(20f / 4f, dash[1])
    }

    @Test
    fun zeroWidthDashDoesNotProduceInfinity() {
        // widthDp == 0 previously divided to Infinity; guard returns a solid line instead.
        assertNull(MapMath.toLibreDashArray(RoutePattern.DASHED, 0f))
        assertNull(MapMath.toLibreDashArray(RoutePattern.DOTTED, 0f))
    }

    // --- fitMarginMaxPx (review #3: the (.../2) - 1 clamp coerced to >= 0) ---

    @Test
    fun fitMarginIsHalfTheSmallerUsableSideMinusOne() {
        val base = PaddingPx(0, 0, 0, 0)
        // smaller usable side = 200 -> (200/2) - 1 = 99
        assertEquals(99, MapMath.fitMarginMaxPx(viewWidth = 400, viewHeight = 200, base = base))
    }

    @Test
    fun fitMarginSubtractsBasePaddingFromUsableArea() {
        val base = PaddingPx(left = 20, top = 10, right = 20, bottom = 10)
        // usableWidth = 400-40 = 360, usableHeight = 200-20 = 180 -> min 180 -> (180/2)-1 = 89
        assertEquals(89, MapMath.fitMarginMaxPx(viewWidth = 400, viewHeight = 200, base = base))
    }

    @Test
    fun fitMarginNeverNegative() {
        val base = PaddingPx(left = 500, top = 0, right = 500, bottom = 0)
        assertEquals(0, MapMath.fitMarginMaxPx(viewWidth = 100, viewHeight = 100, base = base))
    }

    // --- iconImageKey (review #3: cache identity; collision reuses the wrong bitmap) ---

    @Test
    fun equalBytesIconsProduceEqualKeys() {
        val a = MapMarkerIcon.Bytes(byteArrayOf(1, 2, 3))
        val b = MapMarkerIcon.Bytes(byteArrayOf(1, 2, 3))
        assertEquals(MapMath.iconImageKey(a), MapMath.iconImageKey(b))
    }

    @Test
    fun differentBytesIconsProduceDifferentKeys() {
        val a = MapMath.iconImageKey(MapMarkerIcon.Bytes(byteArrayOf(1, 2, 3)))
        val b = MapMath.iconImageKey(MapMarkerIcon.Bytes(byteArrayOf(1, 2, 4)))
        assertTrue(a != b)
    }

    @Test
    fun pinKeyDistinguishesColorAndLabel() {
        val red = MapMath.iconImageKey(MapMarkerIcon.Pin(colorArgb = 0xFFFF0000.toInt(), label = "A"))
        val redB = MapMath.iconImageKey(MapMarkerIcon.Pin(colorArgb = 0xFFFF0000.toInt(), label = "B"))
        val blue = MapMath.iconImageKey(MapMarkerIcon.Pin(colorArgb = 0xFF0000FF.toInt(), label = "A"))
        assertTrue(red != redB)
        assertTrue(red != blue)
    }

    @Test
    fun resourceAndPinKeysAreNamespacedApart() {
        val res = MapMath.iconImageKey(MapMarkerIcon.Resource("car"))
        val pin = MapMath.iconImageKey(MapMarkerIcon.Pin(colorArgb = 0))
        assertTrue(res.startsWith("yalla-icon-res-"))
        assertTrue(pin.startsWith("yalla-icon-pin-"))
    }

    // --- posesClose (review #1: the settled-pose skip that keeps a parked car off the write path) ---

    @Test
    fun identicalPosesAreClose() {
        assertTrue(MapMath.posesClose(41.3, 69.2, 90f, 41.3, 69.2, 90f))
    }

    @Test
    fun subEpsilonDriftIsClose() {
        // < 1e-6 deg (~0.1m) and < 0.1 deg bearing are visually identical -> skip the write.
        assertTrue(MapMath.posesClose(41.3, 69.2, 90f, 41.3 + 5e-7, 69.2 + 5e-7, 90.05f))
    }

    @Test
    fun pastEpsilonMoveIsNotClose() {
        assertFalse(MapMath.posesClose(41.3, 69.2, 90f, 41.3 + 1e-5, 69.2, 90f))
    }

    @Test
    fun pastEpsilonBearingIsNotClose() {
        assertFalse(MapMath.posesClose(41.3, 69.2, 90f, 41.3, 69.2, 90.5f))
    }
}
