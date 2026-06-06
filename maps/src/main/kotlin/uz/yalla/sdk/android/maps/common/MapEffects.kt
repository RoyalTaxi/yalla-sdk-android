package uz.yalla.sdk.android.maps.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import uz.yalla.core.geo.GeoPoint
import uz.yalla.core.location.LocationProvider
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.model.CenterPinState
import uz.yalla.maps.config.MapConstants

@Composable
internal fun LocationTrackingEffect(
    locationProvider: LocationProvider,
    hasPermission: Boolean
) {
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            locationProvider.startTracking()
        }
    }
}

@Composable
internal fun CameraInitializationEffect(
    pendingTarget: GeoPoint?,
    userLocation: GeoPoint?,
    hasCachedLocation: Boolean,
    controller: MapController,
    onInitialized: (isUserLocation: Boolean) -> Unit,
    onCenterPinChanged: ((CenterPinState) -> Unit)?,
    onMapReady: (() -> Unit)?
) {
    LaunchedEffect(pendingTarget) {
        pendingTarget ?: return@LaunchedEffect

        controller.moveTo(pendingTarget, MapConstants.DEFAULT_ZOOM.toFloat())
        notifyMarkerChanged(controller, pendingTarget, onCenterPinChanged)

        val isUserLocation = userLocation != null && userLocation == pendingTarget
        val isValidInitialPosition = isUserLocation || (userLocation == null && hasCachedLocation)
        if (isValidInitialPosition) {
            onMapReady?.invoke()
        }

        onInitialized(isUserLocation)
    }
}

internal fun notifyMarkerChanged(
    controller: MapController,
    point: GeoPoint,
    onCenterPinChanged: ((CenterPinState) -> Unit)?
) {
    val state = CenterPinState(point, isMoving = false, isByUser = false)
    controller.updateCenterPin(state)
    onCenterPinChanged?.invoke(state)
}
