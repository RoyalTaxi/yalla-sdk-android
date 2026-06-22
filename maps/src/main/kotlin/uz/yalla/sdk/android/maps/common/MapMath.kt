package uz.yalla.sdk.android.maps.common

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import uz.yalla.maps.api.model.Anchor
import uz.yalla.maps.api.model.MapMarkerIcon
import uz.yalla.maps.api.model.RoutePattern

internal object MapMath {

    const val BASE_METERS_PER_PIXEL = 156543.03392

    const val CIRCLE_RADIUS_SCALE = 4_194_304f

    const val CIRCLE_RADIUS_MAX_ZOOM = 22

    fun circleRadiusPixelsAtZoomZero(radiusMeters: Double, lat: Double): Float =
        (radiusMeters / (BASE_METERS_PER_PIXEL * cos(lat * PI / 180.0))).toFloat()

    fun toLibreAnchor(anchor: Anchor): String = when {
        anchor.y >= 0.9f && anchor.x in 0.4f..0.6f -> "bottom"
        anchor.y <= 0.1f && anchor.x in 0.4f..0.6f -> "top"
        anchor.x <= 0.1f -> "left"
        anchor.x >= 0.9f -> "right"
        else -> "center"
    }

    fun toLibreDashArray(pattern: RoutePattern, widthDp: Float): Array<Float>? = when (pattern) {
        RoutePattern.SOLID -> null
        RoutePattern.DASHED -> if (widthDp <= 0f) null else arrayOf(30f / widthDp, 20f / widthDp)
        RoutePattern.DOTTED -> if (widthDp <= 0f) null else arrayOf(0f, 20f / widthDp)
    }

    fun fitMarginMaxPx(viewWidth: Int, viewHeight: Int, base: PaddingPx): Int {
        val usableWidth = viewWidth - base.left - base.right
        val usableHeight = viewHeight - base.top - base.bottom
        return ((minOf(usableWidth, usableHeight) / 2) - 1).coerceAtLeast(0)
    }

    fun iconImageKey(icon: MapMarkerIcon): String = when (icon) {
        is MapMarkerIcon.Resource -> "yalla-icon-res-${icon.name}"
        is MapMarkerIcon.Bytes ->
            "yalla-icon-bytes-${icon.data.contentHashCode().toUInt().toString(16)}"
        is MapMarkerIcon.Pin ->
            "yalla-icon-pin-${icon.colorArgb.toUInt().toString(16)}-${icon.label.orEmpty().hashCode().toUInt().toString(16)}"
        is MapMarkerIcon.Dot ->
            "yalla-icon-dot-${icon.fillColorArgb.toUInt().toString(16)}-${icon.strokeColorArgb.toUInt().toString(16)}-${icon.diameterDp}-${icon.strokeWidthDp}"
    }

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
