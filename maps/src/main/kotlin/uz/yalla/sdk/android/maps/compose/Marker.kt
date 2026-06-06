package uz.yalla.sdk.android.maps.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.maps.android.compose.Marker as AndroidMarker
import com.google.maps.android.compose.rememberUpdatedMarkerState as androidRememberUpdatedMarkerState

@Composable
@GoogleMapComposable
fun Marker(
    state: MarkerState,
    icon: BitmapDescriptor? = null,
    anchor: Offset = Offset(0.5f, 1.0f),
    flat: Boolean = false,
    rotation: Float = 0f,
    clickable: Boolean = true
) {
    val androidState = androidRememberUpdatedMarkerState(
        position = GoogleLatLng(state.position.latitude, state.position.longitude)
    )

    LaunchedEffect(state.position) {
        val newPosition = GoogleLatLng(state.position.latitude, state.position.longitude)
        if (androidState.position != newPosition) {
            androidState.position = newPosition
        }
    }

    AndroidMarker(
        state = androidState,
        anchor = anchor,
        flat = flat,
        icon = icon?.googleBitmapDescriptor,
        rotation = rotation,
        onClick = { !clickable }
    )
}
