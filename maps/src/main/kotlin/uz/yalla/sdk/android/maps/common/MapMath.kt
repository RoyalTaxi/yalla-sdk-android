package uz.yalla.sdk.android.maps.common

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import uz.yalla.maps.api.model.Anchor
import uz.yalla.maps.api.model.MapMarkerIcon
import uz.yalla.maps.api.model.RoutePattern

/**
 * Pure, framework-free map math shared by the Android renderers. Everything here is a value function
 * with no Android/Maps-SDK dependency so it is unit-testable on the JVM (the renderers' boundary math
 * is buyer-visible correctness — anchor classification, the circle-radius zoom curve, dash arrays,
 * the fit-margin clamp, icon-cache identity). Keep new pure logic here, not inline in the controllers.
 */
internal object MapMath {

    /** Web-Mercator ground resolution (metres per pixel) at the equator, zoom 0, 256px tiles. */
    const val BASE_METERS_PER_PIXEL = 156543.03392

    /** Web-Mercator scale factor between zoom 0 and [CIRCLE_RADIUS_MAX_ZOOM] (2^22). */
    const val CIRCLE_RADIUS_SCALE = 4_194_304f // 2f.pow(22); kept literal for iOS parity (4194304.0)

    /** Highest zoom stop used by [circleRadiusPixelsAtZoomZero]'s interpolation. */
    const val CIRCLE_RADIUS_MAX_ZOOM = 22

    /**
     * Pixel radius of a ground circle of [radiusMeters] at zoom 0 and the given [lat] (degrees).
     * The renderers interpolate exponentially from this value at zoom 0 up to
     * `value * CIRCLE_RADIUS_SCALE` at zoom 22, matching iOS (`LibreMapRenderer.swift:474-477`).
     */
    fun circleRadiusPixelsAtZoomZero(radiusMeters: Double, lat: Double): Float =
        (radiusMeters / (BASE_METERS_PER_PIXEL * cos(lat * PI / 180.0))).toFloat()

    /**
     * Maps a fractional [Anchor] to the MapLibre anchor keyword. Off-grid anchors round to the
     * nearest named position; anything central falls through to "center".
     */
    fun toLibreAnchor(anchor: Anchor): String = when {
        anchor.y >= 0.9f && anchor.x in 0.4f..0.6f -> "bottom"
        anchor.y <= 0.1f && anchor.x in 0.4f..0.6f -> "top"
        anchor.x <= 0.1f -> "left"
        anchor.x >= 0.9f -> "right"
        else -> "center"
    }

    /**
     * Dash array (in line-width units) for a MapLibre line, or null for a solid line. Guards against
     * a zero [widthDp] (which would divide to Infinity) by returning a solid line.
     */
    fun toLibreDashArray(pattern: RoutePattern, widthDp: Float): Array<Float>? = when (pattern) {
        RoutePattern.SOLID -> null
        RoutePattern.DASHED -> if (widthDp <= 0f) null else arrayOf(30f / widthDp, 20f / widthDp)
        RoutePattern.DOTTED -> if (widthDp <= 0f) null else arrayOf(0f, 20f / widthDp)
    }

    /**
     * Largest framing margin (px) that still leaves a positive viewport after base padding, for the
     * Google `newLatLngBounds(bounds, margin)` call. Never negative.
     */
    fun fitMarginMaxPx(viewWidth: Int, viewHeight: Int, base: PaddingPx): Int {
        val usableWidth = viewWidth - base.left - base.right
        val usableHeight = viewHeight - base.top - base.bottom
        return ((minOf(usableWidth, usableHeight) / 2) - 1).coerceAtLeast(0)
    }

    /**
     * Stable cache identity for a marker [icon]. Equal icons must produce equal keys (the GL image
     * table and bitmap cache are keyed by this); a collision silently reuses the wrong bitmap.
     */
    fun iconImageKey(icon: MapMarkerIcon): String = when (icon) {
        is MapMarkerIcon.Resource -> "yalla-icon-res-${icon.name}"
        is MapMarkerIcon.Bytes ->
            "yalla-icon-bytes-${icon.data.contentHashCode().toUInt().toString(16)}"
        is MapMarkerIcon.Pin ->
            "yalla-icon-pin-${icon.colorArgb.toUInt().toString(16)}-${icon.label.orEmpty().hashCode().toUInt().toString(16)}"
        is MapMarkerIcon.Dot ->
            "yalla-icon-dot-${icon.fillColorArgb.toUInt().toString(16)}-${icon.strokeColorArgb.toUInt().toString(16)}-${icon.diameterDp}-${icon.strokeWidthDp}"
    }

    /**
     * True when two poses are within the renderers' write epsilon (~0.1m / 0.1°) and so visually
     * identical — a settled marker that samples the same pose every frame must NOT be re-written.
     * Mirrors iOS `MarkerMotionDriver.posesClose` (`MarkerMotionDriver.swift:77-81`).
     */
    fun posesClose(
        aLat: Double,
        aLng: Double,
        aBearing: Float,
        bLat: Double,
        bLng: Double,
        bBearing: Float
    ): Boolean =
        abs(aLat - bLat) < 1e-6 &&
            abs(aLng - bLng) < 1e-6 &&
            abs(aBearing - bBearing) < 0.1f
}
