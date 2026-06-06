package uz.yalla.sdk.android.maps.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uz.yalla.sdk.android.maps.model.CameraPosition
import uz.yalla.sdk.android.maps.model.LatLng
import uz.yalla.maps.util.hasSameValues
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.LatLngBounds as GoogleLatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason as GoogleCameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState as GoogleCameraPositionState

@Composable
internal fun rememberSyncedGoogleCameraPositionState(
    cameraPositionState: CameraPositionState,
    contentPadding: PaddingValues
): GoogleCameraPositionState {
    val googleCameraPositionState = remember {
        GoogleCameraPositionState(
            position = cameraPositionState.position.toGoogleCameraPosition()
        )
    }

    DisposableEffect(googleCameraPositionState) {
        cameraPositionState.positionUpdater = { position ->
            googleCameraPositionState.position = position.toGoogleCameraPosition()
            cameraPositionState.rawPosition = position
        }
        onDispose {
            cameraPositionState.positionUpdater = null
        }
    }

    LaunchedEffect(googleCameraPositionState.isMoving) {
        cameraPositionState.isMoving = googleCameraPositionState.isMoving
        if (!googleCameraPositionState.isMoving) {
            cameraPositionState.rawPosition = googleCameraPositionState.position.toCameraPosition()
        }
    }

    LaunchedEffect(googleCameraPositionState.cameraMoveStartedReason) {
        cameraPositionState.cameraMoveStartedReason = googleCameraPositionState.cameraMoveStartedReason.toCameraMoveStartedReason()
    }

    LaunchedEffect(Unit) {
        var activeJob: Job? = null
        cameraPositionState.animationRequests.collect { request ->
            activeJob?.cancel()
            activeJob = launch {
                when (request) {
                    is CameraAnimationRequest.ToPosition -> {
                        googleCameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(request.position.toGoogleCameraPosition()),
                            request.durationMs
                        )
                    }
                    is CameraAnimationRequest.ToBounds -> {
                        val bounds = GoogleLatLngBounds
                            .Builder()
                            .include(request.bounds.southwest.toGoogleLatLng())
                            .include(request.bounds.northeast.toGoogleLatLng())
                            .build()
                        googleCameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, request.padding),
                            request.durationMs
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        cameraPositionState.moveRequests.collect { position ->
            googleCameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(position.toGoogleCameraPosition())
            )
            cameraPositionState.rawPosition = position
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    var previousPadding by remember { mutableStateOf(contentPadding) }
    LaunchedEffect(contentPadding, layoutDirection) {
        if (!contentPadding.hasSameValues(previousPadding, layoutDirection)) {
            googleCameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(googleCameraPositionState.position)
            )
            previousPadding = contentPadding
        }
    }

    return googleCameraPositionState
}

internal fun CameraPosition.toGoogleCameraPosition() = GoogleCameraPosition(
    target.toGoogleLatLng(),
    zoom,
    tilt,
    bearing
)

private fun GoogleCameraPosition.toCameraPosition() = CameraPosition(
    target = LatLng(target.latitude, target.longitude),
    zoom = zoom,
    tilt = tilt,
    bearing = bearing
)

fun LatLng.toGoogleLatLng() = GoogleLatLng(latitude, longitude)

private fun GoogleCameraMoveStartedReason.toCameraMoveStartedReason() = when (this) {
    GoogleCameraMoveStartedReason.GESTURE -> CameraMoveStartedReason.GESTURE
    GoogleCameraMoveStartedReason.API_ANIMATION -> CameraMoveStartedReason.API_ANIMATION
    GoogleCameraMoveStartedReason.DEVELOPER_ANIMATION -> CameraMoveStartedReason.DEVELOPER_ANIMATION
    else -> CameraMoveStartedReason.UNKNOWN
}
