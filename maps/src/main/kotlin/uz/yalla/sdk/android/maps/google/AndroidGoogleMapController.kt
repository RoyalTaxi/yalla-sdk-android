package uz.yalla.sdk.android.maps.google

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition as GmsCameraPosition
import com.google.android.gms.maps.model.Circle as GmsCircle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker as GmsMarker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline as GmsPolyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.yalla.core.geo.GeoPoint
import uz.yalla.maps.api.AndroidMapController
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.model.CameraPosition
import uz.yalla.maps.api.model.CenterPinState
import uz.yalla.maps.api.model.MapCircle
import uz.yalla.maps.api.model.MapEvent
import uz.yalla.maps.api.model.MapMarker
import uz.yalla.maps.api.model.MapRoute
import uz.yalla.maps.api.model.MapStyle
import uz.yalla.maps.api.model.RoutePattern
import uz.yalla.maps.config.MapConstants
import uz.yalla.sdk.android.maps.common.MarkerIconLoader
import uz.yalla.sdk.android.maps.common.toPaddingPx

internal class AndroidGoogleMapController(
    private val applicationContext: Context
) : MapController, AndroidMapController {

    private val _cameraPosition = MutableStateFlow(CameraPosition.DEFAULT)
    override val cameraPosition = _cameraPosition.asStateFlow()

    private val _centerPin = MutableStateFlow(CenterPinState.INITIAL)
    override val centerPin = _centerPin.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    override val isReady = _isReady.asStateFlow()

    private val _events = MutableSharedFlow<MapEvent>(replay = 0, extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val events = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var lifecycleObserver: LifecycleEventObserver? = null
    private var attachedLifecycle: Lifecycle? = null
    private var closed = false
    private var lastEmittedCamera: CameraPosition? = null

    private var pendingMarkers: List<MapMarker> = emptyList()
    private var pendingRoutes: List<MapRoute> = emptyList()
    private var pendingCircles: List<MapCircle> = emptyList()
    private var pendingPadding: PaddingValues = PaddingValues()
    private var userInitiatedMove = false
    private var lockedTarget: GeoPoint? = null
    private var lockedZoom: Float? = null

    private val renderedMarkers = HashMap<String, GmsMarker>()
    private val renderedRoutes = HashMap<String, GmsPolyline>()
    private val renderedCircles = HashMap<String, GmsCircle>()
    private val markerData = HashMap<String, MapMarker>()
    private val routeData = HashMap<String, MapRoute>()
    private val circleData = HashMap<String, MapCircle>()

    override fun createView(context: Context, lifecycle: Lifecycle): View {
        ensureMainThread()
        check(!closed) { "AndroidGoogleMapController is closed; create a new controller." }
        detachFromLifecycle()
        val view = MapView(context).apply { onCreate(Bundle()) }
        mapView = view
        attachedLifecycle = lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                Lifecycle.Event.ON_DESTROY -> detach()
                else -> Unit
            }
        }
        lifecycleObserver = observer
        lifecycle.addObserver(observer)
        view.getMapAsync { gm -> onMapReady(gm) }
        return view
    }

    override fun detach() {
        ensureMainThread()
        detachFromLifecycle()
        mapView?.also { mv ->
            mv.onPause()
            mv.onStop()
            mv.onDestroy()
        }
        renderedMarkers.values.forEach { it.remove() }
        renderedRoutes.values.forEach { it.remove() }
        renderedCircles.values.forEach { it.remove() }
        renderedMarkers.clear()
        renderedRoutes.clear()
        renderedCircles.clear()
        markerData.clear()
        routeData.clear()
        circleData.clear()
        mapView = null
        googleMap = null
        _isReady.value = false
    }

    private fun detachFromLifecycle() {
        val obs = lifecycleObserver
        val lc = attachedLifecycle
        if (obs != null && lc != null) lc.removeObserver(obs)
        lifecycleObserver = null
        attachedLifecycle = null
    }

    private fun onMapReady(gm: GoogleMap) {
        googleMap = gm
        applyPadding(pendingPadding)
        renderMarkers(pendingMarkers)
        renderRoutes(pendingRoutes)
        renderCircles(pendingCircles)
        gm.setOnCameraMoveStartedListener { reason ->
            userInitiatedMove = reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
            if (userInitiatedMove) {
                lockedTarget = null
                lockedZoom = null
            }
        }
        gm.setOnCameraMoveListener {
            val pos = gm.cameraPosition
            emitCamera(pos.toShared(pendingPadding))
            _centerPin.value = _centerPin.value.copy(
                point = pos.target.toGeoPoint(),
                isMoving = true,
                isByUser = userInitiatedMove
            )
        }
        gm.setOnCameraIdleListener {
            val pos = gm.cameraPosition
            emitCamera(pos.toShared(pendingPadding))
            _centerPin.value = CenterPinState(
                point = pos.target.toGeoPoint(),
                isMoving = false,
                isByUser = userInitiatedMove
            )
            userInitiatedMove = false
        }
        gm.setOnMarkerClickListener { gmsMarker ->
            val id = renderedMarkers.entries.firstOrNull { it.value === gmsMarker }?.key
            if (id != null) scope.launch { _events.emit(MapEvent.MarkerTapped(id)) }
            false
        }
        gm.setOnMapClickListener { latLng ->
            scope.launch { _events.emit(MapEvent.MapTapped(latLng.toGeoPoint())) }
        }
        gm.setOnMapLongClickListener { latLng ->
            scope.launch { _events.emit(MapEvent.MapLongPressed(latLng.toGeoPoint())) }
        }
        _isReady.value = true
    }

    override suspend fun moveTo(point: GeoPoint, zoom: Float) {
        val gm = googleMap ?: return
        gm.moveCamera(CameraUpdateFactory.newLatLngZoom(point.toLatLng(), zoom.clampZoom()))
    }

    override suspend fun animateTo(point: GeoPoint, zoom: Float, durationMs: Int) {
        animateCamera(CameraUpdateFactory.newLatLngZoom(point.toLatLng(), zoom.clampZoom()), durationMs)
    }

    override suspend fun animateToWithBearing(point: GeoPoint, bearing: Float, zoom: Float, durationMs: Int) {
        val current = googleMap?.cameraPosition ?: return
        val target = GmsCameraPosition.Builder()
            .target(point.toLatLng())
            .zoom(zoom.clampZoom())
            .bearing(bearing)
            .tilt(current.tilt)
            .build()
        animateCamera(CameraUpdateFactory.newCameraPosition(target), durationMs)
    }

    override suspend fun fitBounds(points: List<GeoPoint>, animate: Boolean) {
        val gm = googleMap ?: return
        val valid = points.filterNot { it == GeoPoint.Zero }.distinctBy { it.lat to it.lng }
        if (valid.isEmpty()) return
        if (valid.size == 1) {
            val single = valid.first()
            val z = gm.cameraPosition.zoom.clampZoom()
            if (animate) animateTo(single, z, MapController.ANIMATION_DURATION) else moveTo(single, z)
            return
        }
        val builder = LatLngBounds.Builder()
        valid.forEach { builder.include(it.toLatLng()) }
        val bounds = builder.build()
        val px = pendingPadding.toPaddingPx(applicationContext)
        val baseMargin = (24 * applicationContext.resources.displayMetrics.density).toInt()
        val visualMarginPx = maxOf(px.left, px.top, px.right, px.bottom) + baseMargin
        val update = CameraUpdateFactory.newLatLngBounds(bounds, visualMarginPx)
        if (animate) animateCamera(update, MapController.ANIMATION_DURATION) else gm.moveCamera(update)
    }

    override suspend fun zoomIn() {
        animateCamera(CameraUpdateFactory.zoomIn(), MapController.ANIMATION_DURATION)
    }

    override suspend fun zoomOut() {
        animateCamera(CameraUpdateFactory.zoomOut(), MapController.ANIMATION_DURATION)
    }

    override suspend fun setZoom(zoom: Float) {
        animateCamera(CameraUpdateFactory.zoomTo(zoom.clampZoom()), MapController.ANIMATION_DURATION)
    }

    override suspend fun setStyle(style: MapStyle, isDark: Boolean) {
        val gm = googleMap ?: return
        when (style) {
            is MapStyle.InlineJson -> {
                val json = if (isDark) style.darkJson else style.lightJson
                gm.setMapStyle(com.google.android.gms.maps.model.MapStyleOptions(json))
            }
            else -> Unit
        }
    }

    override fun setDesiredPadding(padding: PaddingValues) {
        ensureMainThread()
        pendingPadding = padding
        applyPadding(padding)
        replayLockedTarget()
    }

    override fun setMarkers(markers: List<MapMarker>) {
        ensureMainThread()
        pendingMarkers = markers
        if (googleMap != null) renderMarkers(markers)
    }

    override fun setRoutes(routes: List<MapRoute>) {
        ensureMainThread()
        pendingRoutes = routes
        if (googleMap != null) renderRoutes(routes)
    }

    override fun setCircles(circles: List<MapCircle>) {
        ensureMainThread()
        pendingCircles = circles
        if (googleMap != null) renderCircles(circles)
    }

    override fun lockTarget(point: GeoPoint, zoom: Float?) {
        ensureMainThread()
        lockedTarget = point
        lockedZoom = zoom
        replayLockedTarget()
    }

    override fun unlockTarget() {
        ensureMainThread()
        lockedTarget = null
        lockedZoom = null
    }

    override fun snapshotScene(): MapController.SceneSnapshot = MapController.SceneSnapshot(
        cameraPosition = _cameraPosition.value,
        markers = pendingMarkers,
        routes = pendingRoutes,
        circles = pendingCircles,
        padding = pendingPadding,
        lockedTarget = lockedTarget,
        lockedZoom = lockedZoom
    )

    override fun close() {
        ensureMainThread()
        if (closed) return
        closed = true
        detach()
        scope.cancel()
    }

    private fun replayLockedTarget() {
        val gm = googleMap ?: return
        val target = lockedTarget ?: return
        val zoom = lockedZoom ?: gm.cameraPosition.zoom
        scope.launch { animateCamera(CameraUpdateFactory.newLatLngZoom(target.toLatLng(), zoom.clampZoom()), 250) }
    }

    private fun applyPadding(padding: PaddingValues) {
        val gm = googleMap ?: return
        val px = padding.toPaddingPx(applicationContext)
        gm.setPadding(px.left, px.top, px.right, px.bottom)
    }

    private fun emitCamera(next: CameraPosition) {
        val prev = lastEmittedCamera
        if (prev != null && cameraEpsilonEqual(prev, next)) return
        lastEmittedCamera = next
        _cameraPosition.value = next
    }

    private fun renderMarkers(markers: List<MapMarker>) {
        val gm = googleMap ?: return
        val incoming = markers.associateBy { it.id }
        val toRemove = renderedMarkers.keys - incoming.keys
        toRemove.forEach { id ->
            renderedMarkers.remove(id)?.remove()
            markerData.remove(id)
        }
        incoming.forEach { (id, marker) ->
            val previous = markerData[id]
            val existing = renderedMarkers[id]
            if (existing == null) {
                val options = MarkerOptions()
                    .position(marker.point.toLatLng())
                    .anchor(marker.anchor.x, marker.anchor.y)
                    .rotation(marker.rotation)
                    .flat(marker.flat)
                    .draggable(false)
                    .zIndex(marker.zIndex)
                    .title(marker.contentDescription)
                marker.icon?.let { MarkerIconLoader.loadGmsDescriptor(applicationContext, it)?.let(options::icon) }
                gm.addMarker(options)?.let { renderedMarkers[id] = it }
            } else {
                if (previous?.point != marker.point) existing.position = marker.point.toLatLng()
                if (previous?.rotation != marker.rotation) existing.rotation = marker.rotation
                if (previous?.flat != marker.flat) existing.isFlat = marker.flat
                if (previous?.anchor != marker.anchor) existing.setAnchor(marker.anchor.x, marker.anchor.y)
                if (previous?.zIndex != marker.zIndex) existing.zIndex = marker.zIndex
                if (previous?.icon != marker.icon) {
                    marker.icon?.let { MarkerIconLoader.loadGmsDescriptor(applicationContext, it)?.let(existing::setIcon) }
                }
                if (previous?.contentDescription != marker.contentDescription) existing.title = marker.contentDescription
            }
            markerData[id] = marker
        }
    }

    private fun renderRoutes(routes: List<MapRoute>) {
        val gm = googleMap ?: return
        val incoming = routes.associateBy { it.id }
        val toRemove = renderedRoutes.keys - incoming.keys
        toRemove.forEach { id ->
            renderedRoutes.remove(id)?.remove()
            routeData.remove(id)
        }
        val density = applicationContext.resources.displayMetrics.density
        incoming.forEach { (id, route) ->
            val previous = routeData[id]
            val existing = renderedRoutes[id]
            if (existing == null) {
                val options = PolylineOptions()
                    .addAll(route.points.map { it.toLatLng() })
                    .color(route.colorArgb)
                    .width(route.widthDp * density)
                    .zIndex(route.zIndex)
                route.pattern.toGmsPattern(density)?.let(options::pattern)
                gm.addPolyline(options).also { renderedRoutes[id] = it }
            } else {
                if (previous?.points != route.points) existing.points = route.points.map { it.toLatLng() }
                if (previous?.colorArgb != route.colorArgb) existing.color = route.colorArgb
                if (previous?.widthDp != route.widthDp) existing.width = route.widthDp * density
                if (previous?.zIndex != route.zIndex) existing.zIndex = route.zIndex
                if (previous?.pattern != route.pattern) existing.pattern = route.pattern.toGmsPattern(density)
            }
            routeData[id] = route
        }
    }

    private fun renderCircles(circles: List<MapCircle>) {
        val gm = googleMap ?: return
        val incoming = circles.associateBy { it.id }
        val toRemove = renderedCircles.keys - incoming.keys
        toRemove.forEach { id ->
            renderedCircles.remove(id)?.remove()
            circleData.remove(id)
        }
        val density = applicationContext.resources.displayMetrics.density
        incoming.forEach { (id, circle) ->
            val previous = circleData[id]
            val existing = renderedCircles[id]
            if (existing == null) {
                val options = CircleOptions()
                    .center(circle.center.toLatLng())
                    .radius(circle.radiusMeters)
                    .fillColor(circle.fillColorArgb)
                    .strokeColor(circle.strokeColorArgb)
                    .strokeWidth(circle.strokeWidthDp * density)
                    .zIndex(circle.zIndex)
                gm.addCircle(options).also { renderedCircles[id] = it }
            } else {
                if (previous?.center != circle.center) existing.center = circle.center.toLatLng()
                if (previous?.radiusMeters != circle.radiusMeters) existing.radius = circle.radiusMeters
                if (previous?.fillColorArgb != circle.fillColorArgb) existing.fillColor = circle.fillColorArgb
                if (previous?.strokeColorArgb != circle.strokeColorArgb) existing.strokeColor = circle.strokeColorArgb
                if (previous?.strokeWidthDp != circle.strokeWidthDp) existing.strokeWidth = circle.strokeWidthDp * density
                if (previous?.zIndex != circle.zIndex) existing.zIndex = circle.zIndex
            }
            circleData[id] = circle
        }
    }

    private suspend fun animateCamera(update: com.google.android.gms.maps.CameraUpdate, durationMs: Int) {
        val gm = googleMap ?: return
        suspendCoroutine<Unit> { cont ->
            val callback = object : GoogleMap.CancelableCallback {
                override fun onFinish() { cont.resume(Unit) }
                override fun onCancel() { cont.resume(Unit) }
            }
            gm.animateCamera(update, durationMs, callback)
        }
    }

    private fun Float.clampZoom(): Float =
        coerceIn(MapConstants.ZOOM_MIN.toFloat(), MapConstants.ZOOM_MAX.toFloat())

    private fun GeoPoint.toLatLng(): LatLng = LatLng(lat, lng)

    private fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

    private fun GmsCameraPosition.toShared(padding: PaddingValues): CameraPosition = CameraPosition(
        target = target.toGeoPoint(),
        zoom = zoom,
        bearing = bearing,
        tilt = tilt,
        padding = padding
    )

    private fun RoutePattern.toGmsPattern(density: Float): List<PatternItem>? = when (this) {
        RoutePattern.SOLID -> null
        RoutePattern.DASHED -> listOf(Dash(30f * density), Gap(20f * density))
        RoutePattern.DOTTED -> listOf(Dot(), Gap(20f * density))
    }

    private fun cameraEpsilonEqual(a: CameraPosition, b: CameraPosition): Boolean {
        return kotlin.math.abs(a.target.lat - b.target.lat) < 1e-6 &&
            kotlin.math.abs(a.target.lng - b.target.lng) < 1e-6 &&
            kotlin.math.abs(a.zoom - b.zoom) < 1e-3 &&
            kotlin.math.abs(a.bearing - b.bearing) < 0.1f &&
            kotlin.math.abs(a.tilt - b.tilt) < 0.1f
    }

    private fun ensureMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "MapController must be called on the Android main thread."
        }
    }
}
