package uz.yalla.sdk.android.maps.google.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import uz.yalla.core.geo.GeoPoint
import uz.yalla.sdk.android.maps.compose.Circle
import uz.yalla.sdk.android.maps.compose.Marker
import uz.yalla.sdk.android.maps.compose.rememberComposeBitmapDescriptor
import uz.yalla.sdk.android.maps.compose.rememberUpdatedMarkerState
import uz.yalla.sdk.android.maps.common.MapColors
import uz.yalla.sdk.android.maps.common.MapDimens
import uz.yalla.sdk.android.maps.common.UserLocationPainter
import uz.yalla.sdk.android.maps.google.toLatLng

@Composable
internal fun LocationIndicator(
    location: GeoPoint?,
    accuracyMeters: Double = MapDimens.DEFAULT_ACCURACY_METERS
) {
    location ?: return

    Circle(
        center = location.toLatLng(),
        radius = accuracyMeters,
        fillColor = MapColors.Primary.copy(alpha = 0.2f),
        strokeColor = MapColors.Primary.copy(alpha = 0.4f),
        strokeWidth = 1f
    )

    val icon = rememberComposeBitmapDescriptor("user-location", MapDimens.UserLocationSize) {
        Canvas(modifier = Modifier.size(MapDimens.UserLocationSize)) {
            with(UserLocationPainter) { draw(size) }
        }
    }
    Marker(
        state = rememberUpdatedMarkerState(position = location.toLatLng()),
        icon = icon,
        anchor = Offset(0.5f, 0.5f)
    )
}
