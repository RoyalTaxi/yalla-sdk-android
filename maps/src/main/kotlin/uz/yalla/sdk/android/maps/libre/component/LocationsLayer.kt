package uz.yalla.sdk.android.maps.libre.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import io.github.dellisd.spatialk.geojson.dsl.featureCollection
import io.github.dellisd.spatialk.geojson.dsl.point
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import uz.yalla.core.geo.GeoPoint
import uz.yalla.sdk.android.maps.common.LocationType
import uz.yalla.sdk.android.maps.common.MapColors
import uz.yalla.sdk.android.maps.common.MapDimens

@Composable
internal fun LocationsLayer(
    locations: List<GeoPoint>,
    startLabel: String? = null,
    endLabel: String? = null
) {
    if (locations.isEmpty()) return

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    locations.forEachIndexed { index, location ->
        val type = when (index) {
            0 -> LocationType.START
            locations.lastIndex -> LocationType.FINISH
            else -> LocationType.POINT
        }

        val badgeText = when (index) {
            0 -> startLabel
            locations.lastIndex -> endLabel
            else -> null
        }

        key("location-${location.lat}-${location.lng}") {
            LocationMarker(
                layerId = "location-marker-$index",
                location = location,
                type = type,
                badgeText = badgeText,
                textMeasurer = textMeasurer,
                density = density
            )
        }
    }
}

@Composable
private fun LocationMarker(
    layerId: String,
    location: GeoPoint,
    type: LocationType,
    badgeText: String?,
    textMeasurer: TextMeasurer,
    density: Density
) {
    val painter = remember(type, badgeText, density) {
        LocationMarkerPainter(badgeText, type, textMeasurer, density)
    }

    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            featureCollection {
                feature(geometry = point(longitude = location.lng, latitude = location.lat))
            }
        )
    )

    SymbolLayer(
        id = layerId,
        source = source,
        iconImage = image(painter),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        iconAnchor = const(SymbolAnchor.Center)
    )
}

private class LocationMarkerPainter(
    private val badgeText: String?,
    type: LocationType,
    textMeasurer: TextMeasurer,
    density: Density
) : Painter() {
    private val markerSize = with(density) { MapDimens.MarkerSize.toPx() }
    private val borderWidth = with(density) { MapDimens.MarkerBorderWidth.toPx() }
    private val badgeHeight = with(density) { MapDimens.BadgeHeight.toPx() }
    private val badgePadding = with(density) { MapDimens.BadgePadding.toPx() }

    private val textStyle = TextStyle(
        color = MapColors.BadgeText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    private val textLayout = badgeText?.let { textMeasurer.measure(it, textStyle) }
    private val badgeWidth = textLayout?.let { it.size.width + badgePadding * 2 } ?: 0f

    private val borderColor = when (type) {
        LocationType.START -> MapColors.Primary
        LocationType.POINT -> MapColors.IntermediateMarker
        LocationType.FINISH -> MapColors.FinishMarker
    }

    override val intrinsicSize: Size
        get() = Size(
            width = maxOf(badgeWidth, markerSize),
            height = if (badgeText != null) badgeHeight * 2 + markerSize else markerSize
        )

    override fun DrawScope.onDraw() {
        val centerX = size.width / 2
        val markerY = if (badgeText != null) badgeHeight else 0f

        textLayout?.let { drawBadge(centerX, it) }
        drawMarker(centerX, markerY)
    }

    private fun DrawScope.drawBadge(
        centerX: Float,
        layout: androidx.compose.ui.text.TextLayoutResult
    ) {
        drawRoundRect(
            color = MapColors.BadgeBackground,
            topLeft = Offset(centerX - badgeWidth / 2, 0f),
            size = Size(badgeWidth, badgeHeight),
            cornerRadius = CornerRadius(badgeHeight / 2)
        )

        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = centerX - layout.size.width / 2,
                y = (badgeHeight - layout.size.height) / 2
            )
        )
    }

    private fun DrawScope.drawMarker(
        centerX: Float,
        topY: Float
    ) {
        val center = Offset(centerX, topY + markerSize / 2)

        drawCircle(color = borderColor, radius = markerSize / 2, center = center)
        drawCircle(color = MapColors.MarkerFill, radius = markerSize / 2 - borderWidth, center = center)
    }
}
