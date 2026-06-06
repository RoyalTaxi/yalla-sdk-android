package uz.yalla.sdk.android.maps.libre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dellisd.spatialk.geojson.Position
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.rememberCameraState
import uz.yalla.core.geo.GeoPoint
import uz.yalla.core.settings.ThemeKind
import uz.yalla.maps.api.LiteMap
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.model.CenterPinState
import uz.yalla.maps.config.MapConstants
import uz.yalla.maps.config.YallaMaps
import uz.yalla.sdk.android.maps.common.LocationTrackingEffect
import uz.yalla.sdk.android.maps.libre.component.LocationIndicator
import uz.yalla.maps.util.toGeoPoint
import org.maplibre.compose.camera.CameraPosition as LibreCameraPosition

internal class LibreLiteMap : LiteMap {
    @Composable
    override fun Content(
        controller: MapController,
        modifier: Modifier,
        initialPoint: GeoPoint?,
        showLocationIndicator: Boolean,
        bindLocationTracker: Boolean,
        onCenterPinChanged: ((CenterPinState) -> Unit)?,
        onMapReady: (() -> Unit)?
    ) {
        val libreController = controller.requireLibreController()

        val dependencies = YallaMaps.current()
        val scope = rememberCoroutineScope()

        val themeType by dependencies.themePreference.collectAsStateWithLifecycle(ThemeKind.System)
        val theme = rememberMapTheme(themeType)
        val currentLocation by dependencies.locationProvider.currentLocation.collectAsStateWithLifecycle(null)
        val fallback = initialPoint ?: MapConstants.BOBUR_SQUARE.toGeoPoint()

        val initialTarget = remember(initialPoint, currentLocation, fallback) {
            val userTarget = currentLocation?.takeIf { it != GeoPoint.Zero }
            initialPoint ?: userTarget ?: fallback
        }

        val cameraState = rememberCameraState(
            firstPosition = LibreCameraPosition(
                target = Position(latitude = initialTarget.lat, longitude = initialTarget.lng),
                zoom = MapConstants.DEFAULT_ZOOM
            )
        )

        var isMapReady by remember { mutableStateOf(false) }
        var hasMovedToUserLocation by remember { mutableStateOf(false) }

        val locationFix = currentLocation?.takeIf { it != GeoPoint.Zero }
        val permissionGranted = locationFix != null

        LaunchedEffect(cameraState) {
            libreController.bind(cameraState, scope)
        }

        LocationTrackingEffect(dependencies.locationProvider, permissionGranted)

        LaunchedEffect(isMapReady, locationFix, initialPoint, permissionGranted) {
            if (!permissionGranted) return@LaunchedEffect
            if (!isMapReady || hasMovedToUserLocation || initialPoint != null) return@LaunchedEffect

            locationFix?.let { target ->
                hasMovedToUserLocation = true
                libreController.moveTo(target, MapConstants.DEFAULT_ZOOM.toFloat())
            }
        }

        CameraTrackingEffect(cameraState, libreController, onCenterPinChanged)

        BaseMapContent(
            cameraState = cameraState,
            theme = theme,
            modifier = modifier,
            onMapReady = {
                libreController.onMapReady()
                onMapReady?.invoke()
                isMapReady = true

                if (initialPoint != null || locationFix == null) {
                    scope.launch {
                        libreController.moveTo(initialTarget, MapConstants.DEFAULT_ZOOM.toFloat())
                    }
                }
            }
        ) {
            if (showLocationIndicator) {
                LocationIndicator(currentLocation, cameraState)
            }
        }
    }
}
