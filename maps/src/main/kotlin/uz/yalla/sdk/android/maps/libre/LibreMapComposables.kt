package uz.yalla.sdk.android.maps.libre

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import uz.yalla.core.geo.GeoPoint
import uz.yalla.core.settings.ThemeKind
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.model.CenterPinState
import uz.yalla.maps.config.MapConstants
import uz.yalla.sdk.android.maps.libre.platform.getDisabledGestures
import uz.yalla.sdk.android.maps.libre.platform.getPlatformGestures
import uz.yalla.sdk.android.maps.libre.platform.getPlatformOrnamentOptions
import uz.yalla.sdk.android.maps.libre.platform.getPlatformRenderOptions

@Composable
internal fun rememberMapTheme(themeType: ThemeKind): ThemeKind {
    val isSystemDark = isSystemInDarkTheme()
    return remember(themeType, isSystemDark) {
        when (themeType) {
            ThemeKind.Light -> ThemeKind.Light
            ThemeKind.Dark -> ThemeKind.Dark
            ThemeKind.System ->
                if (isSystemDark) ThemeKind.Dark else ThemeKind.Light
        }
    }
}

private fun ThemeKind.toMapStyle(): BaseStyle.Uri = BaseStyle.Uri(
    when (this) {
        ThemeKind.Dark -> MapConstants.DARK_STYLE_URL
        else -> MapConstants.LIGHT_STYLE_URL
    }
)

@Composable
internal fun BaseMapContent(
    cameraState: CameraState,
    theme: ThemeKind,
    modifier: Modifier = Modifier,
    gesturesEnabled: Boolean = true,
    onMapReady: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val gestureOptions = remember(gesturesEnabled) {
        if (gesturesEnabled) getPlatformGestures() else getDisabledGestures()
    }

    MaplibreMap(
        cameraState = cameraState,
        zoomRange = MapConstants.ZOOM_MIN.toFloat()..MapConstants.ZOOM_MAX.toFloat(),
        boundingBox = MapConstants.UZBEKISTAN_BOUNDING_BOX,
        options = MapOptions(
            gestureOptions = gestureOptions,
            renderOptions = getPlatformRenderOptions(),
            ornamentOptions = getPlatformOrnamentOptions()
        ),
        onMapLoadFinished = onMapReady,
        baseStyle = theme.toMapStyle(),
        modifier = modifier,
        content = content
    )
}

@Composable
internal fun CameraTrackingEffect(
    cameraState: CameraState,
    controller: LibreMapController,
    onCenterPinChanged: ((CenterPinState) -> Unit)?
) {
    LaunchedEffect(Unit) {
        snapshotFlow { cameraState.isCameraMoving }
            .collectLatest { isMoving ->
                val isByUser = cameraState.moveReason == CameraMoveReason.GESTURE
                val suppressMarkerUpdate = controller.shouldSuppressMarkerUpdate(
                    isMoving = isMoving,
                    isByUser = isByUser
                )

                if (isByUser) {
                    controller.onUserGesture()
                }

                if (!isMoving) {
                    controller.onCameraIdle()
                }

                val target = cameraState.position.target
                val state = CenterPinState(
                    point = GeoPoint(target.latitude, target.longitude),
                    isMoving = isMoving,
                    isByUser = isByUser
                )
                controller.updateFromCamera(cameraState.position)
                if (!suppressMarkerUpdate) {
                    controller.updateCenterPin(state)
                    onCenterPinChanged?.invoke(state)
                }
            }
    }
}

internal fun MapController.requireLibreController(): LibreMapController = when (this) {
    is LibreMapController -> this
    is uz.yalla.maps.provider.SwitchingMapController -> libreController as LibreMapController
    else -> error("Expected LibreMapController or SwitchingMapController, got ${this::class.simpleName}")
}
