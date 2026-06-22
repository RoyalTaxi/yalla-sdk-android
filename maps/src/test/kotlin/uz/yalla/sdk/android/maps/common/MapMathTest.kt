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

class MapMathTest {

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

    @Test
    fun circleRadiusAtEquatorMatchesGroundResolution() {
        val expected = (50.0 / MapMath.BASE_METERS_PER_PIXEL).toFloat()
        assertEquals(expected, MapMath.circleRadiusPixelsAtZoomZero(50.0, 0.0), 1e-9f)
    }

    @Test
    fun circleRadiusGrowsWithLatitudeAsMetersPerPixelShrinks() {
        val atEquator = MapMath.circleRadiusPixelsAtZoomZero(50.0, 0.0)
        val atSixty = MapMath.circleRadiusPixelsAtZoomZero(50.0, 60.0)
        assertTrue(atSixty > atEquator)
        assertTrue(abs(atSixty - atEquator * 2f) < atEquator * 0.01f)
    }

    @Test
    fun circleRadiusScaleMatchesIosTwoToThe22() {
        assertEquals(4_194_304f, MapMath.CIRCLE_RADIUS_SCALE)
        assertEquals(22, MapMath.CIRCLE_RADIUS_MAX_ZOOM)
    }

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
        assertNull(MapMath.toLibreDashArray(RoutePattern.DASHED, 0f))
        assertNull(MapMath.toLibreDashArray(RoutePattern.DOTTED, 0f))
    }

    @Test
    fun fitMarginIsHalfTheSmallerUsableSideMinusOne() {
        val base = PaddingPx(0, 0, 0, 0)
        assertEquals(99, MapMath.fitMarginMaxPx(viewWidth = 400, viewHeight = 200, base = base))
    }

    @Test
    fun fitMarginSubtractsBasePaddingFromUsableArea() {
        val base = PaddingPx(left = 20, top = 10, right = 20, bottom = 10)
        assertEquals(89, MapMath.fitMarginMaxPx(viewWidth = 400, viewHeight = 200, base = base))
    }

    @Test
    fun fitMarginNeverNegative() {
        val base = PaddingPx(left = 500, top = 0, right = 500, bottom = 0)
        assertEquals(0, MapMath.fitMarginMaxPx(viewWidth = 100, viewHeight = 100, base = base))
    }

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

    @Test
    fun identicalPosesAreClose() {
        assertTrue(MapMath.posesClose(41.3, 69.2, 90f, 41.3, 69.2, 90f))
    }

    @Test
    fun subEpsilonDriftIsClose() {
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
