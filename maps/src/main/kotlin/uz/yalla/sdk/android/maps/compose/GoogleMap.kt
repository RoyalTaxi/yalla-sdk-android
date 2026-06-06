package uz.yalla.sdk.android.maps.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uz.yalla.core.settings.ThemeKind
import uz.yalla.sdk.android.maps.model.MapProperties
import uz.yalla.sdk.android.maps.model.MapType
import uz.yalla.sdk.android.maps.model.MapUiSettings
import com.google.maps.android.compose.ComposeMapColorScheme as GoogleMapColorScheme
import com.google.maps.android.compose.GoogleMap as AndroidGoogleMap
import com.google.maps.android.compose.MapProperties as GoogleMapProperties
import com.google.maps.android.compose.MapType as GoogleMapType
import com.google.maps.android.compose.MapUiSettings as GoogleMapUiSettings

@Composable
fun GoogleMap(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    theme: ThemeKind,
    contentPadding: PaddingValues,
    onMapLoaded: (() -> Unit)?,
    content: (
        @Composable
        @GoogleMapComposable () -> Unit
    )?
) {
    val googleCameraPositionState = rememberSyncedGoogleCameraPositionState(
        cameraPositionState = cameraPositionState,
        contentPadding = contentPadding
    )

    val googleMapType = when (properties.mapType) {
        MapType.NONE -> GoogleMapType.NONE
        MapType.NORMAL -> GoogleMapType.NORMAL
        MapType.SATELLITE -> GoogleMapType.SATELLITE
        MapType.HYBRID -> GoogleMapType.HYBRID
        MapType.TERRAIN -> GoogleMapType.TERRAIN
    }
    val mapColorScheme = when (theme) {
        ThemeKind.Light -> GoogleMapColorScheme.LIGHT
        ThemeKind.Dark -> GoogleMapColorScheme.DARK
        ThemeKind.System -> GoogleMapColorScheme.FOLLOW_SYSTEM
    }

    AndroidGoogleMap(
        modifier = modifier,
        cameraPositionState = googleCameraPositionState,
        contentPadding = contentPadding,
        mapColorScheme = mapColorScheme,
        properties = GoogleMapProperties(
            isBuildingEnabled = properties.isBuildingEnabled,
            isIndoorEnabled = properties.isIndoorEnabled,
            isMyLocationEnabled = properties.isMyLocationEnabled,
            isTrafficEnabled = properties.isTrafficEnabled,
            mapType = googleMapType,
            minZoomPreference = properties.minZoomPreference,
            maxZoomPreference = properties.maxZoomPreference
        ),
        uiSettings = GoogleMapUiSettings(
            compassEnabled = uiSettings.compassEnabled,
            indoorLevelPickerEnabled = uiSettings.indoorLevelPickerEnabled,
            mapToolbarEnabled = uiSettings.mapToolbarEnabled,
            myLocationButtonEnabled = uiSettings.myLocationButtonEnabled,
            rotationGesturesEnabled = uiSettings.rotationGesturesEnabled,
            scrollGesturesEnabled = uiSettings.scrollGesturesEnabled,
            scrollGesturesEnabledDuringRotateOrZoom = uiSettings.scrollGesturesEnabledDuringRotateOrZoom,
            tiltGesturesEnabled = uiSettings.tiltGesturesEnabled,
            zoomControlsEnabled = uiSettings.zoomControlsEnabled,
            zoomGesturesEnabled = uiSettings.zoomGesturesEnabled
        ),
        onMapLoaded = onMapLoaded,
        content = {
            content?.invoke()
        }
    )
}
