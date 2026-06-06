package uz.yalla.sdk.android.maps.libre

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dellisd.spatialk.geojson.Position
import org.maplibre.compose.camera.rememberCameraState
import uz.yalla.core.geo.GeoPoint
import uz.yalla.core.settings.ThemeKind
import uz.yalla.maps.api.StaticMap
import uz.yalla.maps.config.MapConstants
import uz.yalla.maps.config.YallaMaps
import uz.yalla.sdk.android.maps.libre.component.LocationsLayer
import uz.yalla.sdk.android.maps.libre.component.RouteLayer
import uz.yalla.maps.util.toBoundingBox
import uz.yalla.maps.util.toGeoPoint
import kotlin.time.Duration.Companion.milliseconds
import org.maplibre.compose.camera.CameraPosition as LibreCameraPosition

internal class LibreStaticMap : StaticMap {
    @Composable
    override fun Content(
        modifier: Modifier,
        route: List<GeoPoint>?,
        locations: List<GeoPoint>?,
        startLabel: String?,
        endLabel: String?,
        onMapReady: (() -> Unit)?
    ) {
        val dependencies = YallaMaps.current()

        val themeType by dependencies.themePreference.collectAsStateWithLifecycle(ThemeKind.System)
        val theme = rememberMapTheme(themeType)

        val fallback = MapConstants.BOBUR_SQUARE.toGeoPoint()

        val cameraState = rememberCameraState(
            firstPosition = LibreCameraPosition(
                target = Position(latitude = fallback.lat, longitude = fallback.lng),
                zoom = MapConstants.DEFAULT_ZOOM
            )
        )

        var isMapReady by remember { mutableStateOf(false) }

        val allPoints = remember(route, locations) {
            buildList {
                route?.let { addAll(it) }
                locations?.let { addAll(it) }
            }.filter { it != GeoPoint.Zero }
        }

        LaunchedEffect(isMapReady, allPoints) {
            if (!isMapReady || allPoints.isEmpty()) return@LaunchedEffect

            if (allPoints.size == 1) {
                val point = allPoints.first()
                cameraState.animateTo(
                    duration = 1.milliseconds,
                    finalPosition = LibreCameraPosition(
                        target = Position(latitude = point.lat, longitude = point.lng),
                        zoom = MapConstants.DEFAULT_ZOOM
                    )
                )
            } else {
                cameraState.animateTo(
                    duration = 1.milliseconds,
                    boundingBox = allPoints.toBoundingBox(),
                    padding = PaddingValues(MapConstants.DEFAULT_PADDING)
                )
            }
        }

        BaseMapContent(
            cameraState = cameraState,
            theme = theme,
            gesturesEnabled = false,
            modifier = modifier,
            onMapReady = {
                isMapReady = true
                onMapReady?.invoke()
            }
        ) {
            if (route != null) {
                RouteLayer(route)
            }

            if (!locations.isNullOrEmpty()) {
                LocationsLayer(
                    locations = locations,
                    startLabel = startLabel,
                    endLabel = endLabel
                )
            }
        }
    }
}
