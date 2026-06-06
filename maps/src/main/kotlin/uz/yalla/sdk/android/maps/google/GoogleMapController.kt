package uz.yalla.sdk.android.maps.google

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.yalla.core.geo.GeoPoint
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.MapController.Companion.ANIMATION_DURATION
import uz.yalla.maps.api.model.CameraPosition
import uz.yalla.maps.api.model.CenterPinState
import uz.yalla.sdk.android.maps.compose.CameraPositionState
import uz.yalla.maps.config.MapConstants
import uz.yalla.sdk.android.maps.model.LatLng
import uz.yalla.maps.util.hasSameValues
import uz.yalla.maps.util.plus
import kotlin.math.roundToInt
import uz.yalla.sdk.android.maps.model.CameraPosition as ComposeCameraPosition

internal class GoogleMapController : MapController {
    private var cameraState: CameraPositionState? = null
    private var coroutineScope: CoroutineScope? = null
    private var screenDensity: Density? = null
    private var viewportSize: IntSize? = null
    private var activeAnimationJob: Job? = null
    private var targetPadding = PaddingValues()

    private var programmaticTarget: LatLng? = null
    private var programmaticZoom: Float? = null

    private var queuedRecenter: RecenterRequest? = null

    private val _contentPadding = MutableStateFlow(PaddingValues())

    val contentPadding = _contentPadding.asStateFlow()

    private val _cameraPosition = MutableStateFlow(CameraPosition.DEFAULT)
    override val cameraPosition = _cameraPosition.asStateFlow()

    private val _centerPin = MutableStateFlow(CenterPinState.INITIAL)
    override val centerPin = _centerPin.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    override val isReady = _isReady.asStateFlow()

    fun bind(
        camera: CameraPositionState,
        scope: CoroutineScope,
        density: Density
    ) {
        if (cameraState !== camera) {
            cancelActiveAnimation()
            clearProgrammaticTarget()
            queuedRecenter = null
        }
        cameraState = camera
        coroutineScope = scope
        screenDensity = density
        _contentPadding.value = targetPadding
    }

    fun setMapSize(size: IntSize) {
        viewportSize = size
    }

    fun updateFromCamera(position: ComposeCameraPosition) {
        _cameraPosition.value = CameraPosition(
            target = position.target.toGeoPoint(),
            zoom = position.zoom,
            bearing = position.bearing,
            tilt = position.tilt,
            padding = _contentPadding.value
        )
    }

    fun onCameraIdle() {
        val camera = cameraState ?: return

        queuedRecenter?.let { request ->
            queuedRecenter = null
            coroutineScope?.launch {
                camera.animate(
                    ComposeCameraPosition(
                        target = request.target,
                        zoom = request.zoom,
                        bearing = camera.position.bearing,
                        tilt = camera.position.tilt
                    ),
                    durationMs = 300
                )
                clearProgrammaticTarget()
                updateFromCamera(camera.position)
            }
            return
        }

        clearProgrammaticTarget()
        syncCameraState(camera)
    }

    fun onUserGesture() {
        clearProgrammaticTarget()
        queuedRecenter = null
    }

    override suspend fun moveTo(
        point: GeoPoint,
        zoom: Float
    ) {
        val camera = cameraState ?: return
        cancelActiveAnimation()
        val target = point.toLatLng()
        val clampedZoom = zoom.clampZoom()
        setProgrammaticTarget(target, clampedZoom)
        camera.position = ComposeCameraPosition(
            target = target,
            zoom = clampedZoom,
            bearing = camera.position.bearing,
            tilt = camera.position.tilt
        )
        updateFromCamera(camera.position)
    }

    override suspend fun animateTo(
        point: GeoPoint,
        zoom: Float,
        durationMs: Int
    ) {
        val camera = cameraState ?: return
        cancelActiveAnimation()
        setProgrammaticTarget(point.toLatLng(), zoom.clampZoom())
        animateCamera(camera, point.toLatLng(), zoom.clampZoom(), durationMs)
    }

    override suspend fun animateToWithBearing(
        point: GeoPoint,
        bearing: Float,
        zoom: Float,
        durationMs: Int
    ) {
        val camera = cameraState ?: return
        cancelActiveAnimation()
        setProgrammaticTarget(point.toLatLng(), zoom.clampZoom())
        camera.animate(
            ComposeCameraPosition(
                target = point.toLatLng(),
                zoom = zoom.clampZoom(),
                bearing = bearing,
                tilt = camera.position.tilt
            ),
            durationMs = durationMs
        )
        updateFromCamera(camera.position)
    }

    override suspend fun fitBounds(
        points: List<GeoPoint>,
        padding: PaddingValues,
        animate: Boolean
    ) {
        val validPoints = points
            .filterNot { it == GeoPoint.Zero }
            .distinctBy { it.lat to it.lng }

        if (validPoints.isEmpty()) return

        val camera = cameraState ?: return

        if (validPoints.size == 1) {
            val singlePoint = validPoints.first()
            if (animate) {
                animateTo(singlePoint, camera.position.zoom.clampZoom(), ANIMATION_DURATION)
            } else {
                moveTo(singlePoint, camera.position.zoom.clampZoom())
            }
            return
        }

        clearProgrammaticTarget()
        queuedRecenter = null

        val bounds = validPoints.toLatLngBounds() ?: return
        val boundsPadding = calculateBoundsPadding(padding)

        cancelActiveAnimation()
        camera.animateToBounds(bounds, boundsPadding, if (animate) ANIMATION_DURATION else 1)
        updateFromCamera(camera.position)
    }

    override suspend fun zoomIn() {
        adjustZoom(delta = 1f)
    }

    override suspend fun zoomOut() {
        adjustZoom(delta = -1f)
    }

    override suspend fun setZoom(zoom: Float) {
        val camera = cameraState ?: return
        cancelActiveAnimation()
        clearProgrammaticTarget()
        camera.animate(camera.position.copy(zoom = zoom.clampZoom()), durationMs = ANIMATION_DURATION)
        updateFromCamera(camera.position)
    }

    override fun setDesiredPadding(padding: PaddingValues) {
        if (targetPadding.hasSameValues(padding) && _contentPadding.value.hasSameValues(padding)) return
        targetPadding = padding
        if (!_contentPadding.value.hasSameValues(padding)) {
            _contentPadding.value = padding
        }
    }

    override suspend fun updatePadding(padding: PaddingValues) {
        if (targetPadding.hasSameValues(padding) && _contentPadding.value.hasSameValues(padding)) return

        targetPadding = padding
        if (!_contentPadding.value.hasSameValues(padding)) {
            _contentPadding.value = padding
        }

        if (programmaticTarget != null) {
            val camera = cameraState ?: return
            queuedRecenter = RecenterRequest(programmaticTarget!!, programmaticZoom ?: camera.position.zoom)
            if (!camera.isMoving) {
                onCameraIdle()
            }
        }
    }

    private fun updatePaddingSilently(padding: PaddingValues) {
        if (targetPadding.hasSameValues(padding) && _contentPadding.value.hasSameValues(padding)) return
        targetPadding = padding
        if (!_contentPadding.value.hasSameValues(padding)) {
            _contentPadding.value = padding
        }
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
        _contentPadding.value = PaddingValues()
    }

    private fun syncCameraState(camera: CameraPositionState) {
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
        target: uz.yalla.sdk.android.maps.model.LatLng,
        zoom: Float
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

    private suspend fun animateCamera(
        camera: CameraPositionState,
        target: uz.yalla.sdk.android.maps.model.LatLng,
        zoom: Float,
        durationMs: Int
    ) {
        camera.animate(
            ComposeCameraPosition(
                target = target,
                zoom = zoom,
                bearing = camera.position.bearing,
                tilt = camera.position.tilt
            ),
            durationMs = durationMs
        )
        updateFromCamera(camera.position)
    }

    private suspend fun adjustZoom(delta: Float) {
        val camera = cameraState ?: return
        val newZoom = (camera.position.zoom + delta).clampZoom()
        if (newZoom != camera.position.zoom) {
            cancelActiveAnimation()
            camera.animate(camera.position.copy(zoom = newZoom), durationMs = ANIMATION_DURATION)
            updateFromCamera(camera.position)
        }
    }

    private fun calculateBoundsPadding(extraPadding: PaddingValues): Int {
        val density = screenDensity ?: return 0
        val totalPadding = extraPadding + PaddingValues(MapConstants.DEFAULT_PADDING)
        val rawPadding = totalPadding.maxPx(density)

        val size = viewportSize ?: return rawPadding
        val content = targetPadding

        val horizontalContent = density.run {
            content.calculateLeftPadding(LayoutDirection.Ltr).toPx() +
                content.calculateRightPadding(LayoutDirection.Ltr).toPx()
        }
        val verticalContent = density.run {
            content.calculateTopPadding().toPx() + content.calculateBottomPadding().toPx()
        }

        val availableWidth = size.width - horizontalContent
        val availableHeight = size.height - verticalContent
        val maxAllowed = (minOf(availableWidth, availableHeight) / 2f - 1f).coerceAtLeast(0f)

        return minOf(rawPadding, maxAllowed.roundToInt())
    }

    private fun PaddingValues.maxPx(density: Density): Int = with(density) {
        maxOf(
            calculateLeftPadding(LayoutDirection.Ltr).toPx(),
            calculateRightPadding(LayoutDirection.Ltr).toPx(),
            calculateTopPadding().toPx(),
            calculateBottomPadding().toPx()
        ).roundToInt()
    }

    private fun Float.clampZoom(): Float = coerceIn(MapConstants.ZOOM_MIN.toFloat(), MapConstants.ZOOM_MAX.toFloat())
}

private data class RecenterRequest(
    val target: LatLng,
    val zoom: Float
)
