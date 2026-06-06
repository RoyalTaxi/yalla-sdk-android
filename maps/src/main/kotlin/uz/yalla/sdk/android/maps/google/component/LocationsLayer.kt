package uz.yalla.sdk.android.maps.google.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uz.yalla.core.geo.GeoPoint
import uz.yalla.sdk.android.maps.compose.Marker
import uz.yalla.sdk.android.maps.compose.rememberComposeBitmapDescriptor
import uz.yalla.sdk.android.maps.compose.rememberUpdatedMarkerState
import uz.yalla.sdk.android.maps.common.LocationType
import uz.yalla.sdk.android.maps.common.MapColors
import uz.yalla.sdk.android.maps.common.MapDimens
import uz.yalla.sdk.android.maps.google.toLatLng

@Composable
internal fun LocationsLayer(
    locations: List<GeoPoint>,
    startLabel: String? = null,
    endLabel: String? = null
) {
    if (locations.isEmpty()) return

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
                location = location,
                type = type,
                badgeText = badgeText
            )
        }
    }
}

@Composable
private fun LocationMarker(
    location: GeoPoint,
    type: LocationType,
    badgeText: String?
) {
    val icon = rememberComposeBitmapDescriptor(type, badgeText.orEmpty()) {
        LocationMarkerContent(type = type, badgeText = badgeText)
    }

    Marker(
        state = rememberUpdatedMarkerState(position = location.toLatLng()),
        icon = icon,
        anchor = Offset(0.5f, 0.5f)
    )
}

@Composable
private fun LocationMarkerContent(
    type: LocationType,
    badgeText: String?
) {
    val borderColor = when (type) {
        LocationType.START -> MapColors.Primary
        LocationType.POINT -> MapColors.IntermediateMarker
        LocationType.FINISH -> MapColors.FinishMarker
    }
    val density = LocalDensity.current
    val borderWidthPx = with(density) { MapDimens.MarkerBorderWidth.toPx() }

    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .height(MapDimens.BadgeHeight)
                    .background(
                        color = MapColors.BadgeBackground,
                        shape = RoundedCornerShape(MapDimens.BadgeHeight / 2)
                    ).padding(horizontal = MapDimens.BadgePadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = TextStyle(
                        color = MapColors.BadgeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Canvas(
            modifier = Modifier.size(MapDimens.MarkerSize)
        ) {
            val radius = size.minDimension / 2f
            drawCircle(color = borderColor, radius = radius)
            val innerRadius = (radius - borderWidthPx).coerceAtLeast(0f)
            if (innerRadius > 0f) {
                drawCircle(color = MapColors.MarkerFill, radius = innerRadius)
            }
        }

        if (badgeText != null) {
            Spacer(modifier = Modifier.height(MapDimens.BadgeHeight))
        }
    }
}
