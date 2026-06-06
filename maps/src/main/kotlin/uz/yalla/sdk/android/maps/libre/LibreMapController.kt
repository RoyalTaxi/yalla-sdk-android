package uz.yalla.sdk.android.maps.libre

import androidx.compose.foundation.layout.PaddingValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraState
import uz.yalla.core.geo.GeoPoint
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.MapController.Companion.ANIMATION_DURATION
import uz.yalla.maps.api.model.CameraPosition
import uz.yalla.maps.api.model.CenterPinState
import uz.yalla.maps.config.MapConstants
import uz.yalla.maps.util.hasSameValues
import uz.yalla.maps.util.plus
import uz.yalla.maps.util.toBoundingBox
import uz.yalla.maps.util.toGeoPoint
import uz.yalla.maps.util.toPosition
import kotlin.time.Duration.Companion.milliseconds
import org.maplibre.compose.camera.CameraPosition as LibreCameraPosition

internal class LibreMapController : MapController {
    private var cameraState: CameraState? = null
    private var coroutineScope: CoroutineScope? = null
    private var activeAnimationJob: Job? = null
    private var targetPadding = PaddingValues()
    private var appliedPadding = PaddingValues()

    private var suppressMarkerSyncUntilIdle = false

    private var skipNextIdleMarkerSync = false

    private var programmaticTarget: io.github.dellisd.spatialk.geojson.Position? = null
    private var programmaticZoom: Double? = null
    private var queuedRecenter: RecenterRequest? = null

    private val _cameraPosition = MutableStateFlow(CameraPosition.DEFAULT)
    override val cameraPosition = _cameraPosition.asStateFlow()

    private val _centerPin = MutableStateFlow(CenterPinState.INITIAL)
    override val centerPin = _centerPin.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    override val isReady = _isReady.asStateFlow()

    fun bind(
        camera: CameraState,
        scope: CoroutineScope
    ) {
        if (cameraState !== camera) {
            cancelActiveAnimation()
            clearProgrammaticTarget()
            queuedRecenter = null
        }
        cameraState = camera
        coroutineScope = scope
        appliedPadding = camera.position.padding
    }

    fun updateFromCamera(position: LibreCameraPosition) {
        appliedPadding = position.padding
        _cameraPosition.value = CameraPosition(
            target = position.target.toGeoPoint(),
            zoom = position.zoom.toFloat(),
            bearing = position.bearing.toFloat(),
            tilt = position.tilt.toFloat(),
            padding = position.padding
        )
    }

    fun onCameraIdle() {
        val camera = cameraState ?: return

        queuedRecenter?.let { request ->
            queuedRecenter = null
            coroutineScope?.launch {
                camera.animateTo(
                    duration = 300.milliseconds,
                    finalPosition = LibreCameraPosition(
                        target = request.target,
                        zoom = request.zoom,
                        bearing = camera.position.bearing,
                        tilt = camera.position.tilt,
                        padding = targetPadding
                    )
                )
                clearProgrammaticTarget()
                updateFromCamera(camera.position)
            }
            return
        }

        if (skipNextIdleMarkerSync) {
            skipNextIdleMarkerSync = false
            updateFromCamera(camera.position)
            return
        }

        clearProgrammaticTarget()
        syncCameraState(camera)
    }

    fun onUserGesture() {
        clearProgrammaticTarget()
        queuedRecenter = null
        suppressMarkerSyncUntilIdle = false
        skipNextIdleMarkerSync = false
    }

    fun shouldSuppressMarkerUpdate(
        isMoving: Boolean,
        isByUser: Boolean
    ): Boolean {
        if (isByUser) return false
        if (!suppressMarkerSyncUntilIdle) return false
        if (!isMoving) {
            suppressMarkerSyncUntilIdle = false
        }
        return true
    }

    override suspend fun moveTo(
        point: GeoPoint,
        zoom: Float
    ) {
        val camera = cameraState ?: return
        cancelActiveAnimation()

        val target = point.toPosition()
        val clampedZoom = zoom.toDouble().clampZoom()
        setProgrammaticTarget(target, clampedZoom)

        camera.animateTo(
            duration = 1.milliseconds,
            finalPosition = LibreCameraPosition(
                target = target,
                zoom = clampedZoom,
                padding = targetPadding
            )
        )
        updateFromCamera(camera.position)
    }

    override suspend fun animateTo(
        point: GeoPoint,
        zoom: Float,
        durationMs: Int
    ) {
        val camera = cameraState ?: return

        val target = point.toPosition()
        val clampedZoom = zoom.toDouble().clampZoom()
        setProgrammaticTarget(target, clampedZoom)

        launchAnimation {
            camera.animateTo(
                duration = durationMs.milliseconds,
                finalPosition = LibreCameraPosition(
                    target = target,
                    zoom = clampedZoom,
                    padding = targetPadding
                )
            )
        }
    }

    override suspend fun animateToWithBearing(
        point: GeoPoint,
        bearing: Float,
        zoom: Float,
        durationMs: Int
    ) {
        val camera = cameraState ?: return

        val target = point.toPosition()
        val clampedZoom = zoom.toDouble().clampZoom()
        setProgrammaticTarget(target, clampedZoom)

        launchAnimation {
            camera.animateTo(
                duration = durationMs.milliseconds,
                finalPosition = LibreCameraPosition(
                    target = target,
                    zoom = clampedZoom,
                    bearing = bearing.toDouble(),
                    padding = targetPadding
                )
            )
        }
    }

    override suspend fun fitBounds(
        points: List<GeoPoint>,
        padding: PaddingValues,
        animate: Boolean
    ) {
        val validPoints = points.filterNot { it == GeoPoint.Zero }
        if (validPoints.isEmpty()) return

        val camera = cameraState ?: return

        if (validPoints.size == 1) {
            val singlePoint = validPoints.first()
            if (animate) {
                animateTo(singlePoint, camera.position.zoom.toFloat(), ANIMATION_DURATION)
            } else {
                moveTo(singlePoint, camera.position.zoom.toFloat())
            }
            return
        }

        clearProgrammaticTarget()
        queuedRecenter = null

        launchAnimation {
            camera.animateTo(
                duration = if (animate) ANIMATION_DURATION.milliseconds else 1.milliseconds,
                boundingBox = validPoints.toBoundingBox(),
                padding = targetPadding + padding + PaddingValues(MapConstants.DEFAULT_PADDING)
            )
        }
    }

    override suspend fun zoomIn() {
        adjustZoom(delta = 1.0)
    }

    override suspend fun zoomOut() {
        adjustZoom(delta = -1.0)
    }

    override suspend fun setZoom(zoom: Float) {
        val camera = cameraState ?: return
        clearProgrammaticTarget()
        launchAnimation {
            camera.animateTo(
                duration = ANIMATION_DURATION.milliseconds,
                finalPosition = camera.position.copy(zoom = zoom.toDouble().clampZoom(), padding = targetPadding)
            )
        }
    }

    override fun setDesiredPadding(padding: PaddingValues) {
        if (targetPadding.hasSameValues(padding)) return
        targetPadding = padding
    }

    override suspend fun updatePadding(padding: PaddingValues) {
        if (targetPadding.hasSameValues(padding) && appliedPadding.hasSameValues(padding)) return
        targetPadding = padding
        val camera = cameraState ?: return

        if (padding.hasSameValues(appliedPadding)) return

        if (programmaticTarget != null) {
            queuedRecenter = RecenterRequest(programmaticTarget!!, programmaticZoom ?: camera.position.zoom)
            if (!camera.isCameraMoving) {
                onCameraIdle()
            }
            return
        }

        applyPaddingToCurrentCamera(camera)
    }

    private fun updatePaddingSilently(padding: PaddingValues) {
        if (targetPadding.hasSameValues(padding)) return
        targetPadding = padding
    }

    override fun updateCenterPin(state: CenterPinState) {
        _centerPin.value = state
    }

    override fun setCenterPin(point: GeoPoint) {
        _centerPin.value = _centerPin.value.copy(point = point)
    }

    override fun clearCenterPin() {
        _centerPin.value = CenterPinState.INITIAL
    }

    override fun onMapReady() {
        _isReady.value = true
    }

    override fun reset() {
        cancelActiveAnimation()
        clearProgrammaticTarget()
        queuedRecenter = null
        _isReady.value = false
        _centerPin.value = CenterPinState.INITIAL
        _cameraPosition.value = CameraPosition.DEFAULT
        targetPadding = PaddingValues()
        appliedPadding = PaddingValues()
        suppressMarkerSyncUntilIdle = false
        skipNextIdleMarkerSync = false
    }

    private fun syncCameraState(camera: CameraState) {
        updateFromCamera(camera.position)
        updateCenterPin(
            CenterPinState(
                point = camera.position.target.toGeoPoint(),
                isMoving = false,
                isByUser = false
            )
        )
    }

    private fun setProgrammaticTarget(
        target: io.github.dellisd.spatialk.geojson.Position,
        zoom: Double
    ) {
        programmaticTarget = target
        programmaticZoom = zoom
    }

    private fun clearProgrammaticTarget() {
        programmaticTarget = null
        programmaticZoom = null
    }

    private fun cancelActiveAnimation() {
        activeAnimationJob?.cancel()
        activeAnimationJob = null
    }

    private fun applyPaddingToCurrentCamera(camera: CameraState) {
        cancelActiveAnimation()
        suppressMarkerSyncUntilIdle = true
        skipNextIdleMarkerSync = true
        camera.position = camera.position.copy(padding = targetPadding)
        updateFromCamera(camera.position)
    }

    private fun launchAnimation(block: suspend () -> Unit) {
        val scope = coroutineScope ?: return
        cancelActiveAnimation()
        activeAnimationJob = scope.launch {
            block()
            cameraState?.let { updateFromCamera(it.position) }
            updateCenterPin(
                CenterPinState(
                    point = cameraState?.position?.target?.toGeoPoint() ?: GeoPoint.Zero,
                    isMoving = false,
                    isByUser = false
                )
            )
        }
    }

    private suspend fun adjustZoom(delta: Double) {
        val camera = cameraState ?: return
        val newZoom = (camera.position.zoom + delta).clampZoom()
        if (newZoom != camera.position.zoom) {
            launchAnimation {
                camera.animateTo(
                    duration = ANIMATION_DURATION.milliseconds,
                    finalPosition = camera.position.copy(zoom = newZoom, padding = targetPadding)
                )
            }
        }
    }

    private fun Double.clampZoom() = coerceIn(MapConstants.ZOOM_MIN, MapConstants.ZOOM_MAX)
}

private data class RecenterRequest(
    val target: io.github.dellisd.spatialk.geojson.Position,
    val zoom: Double
)
