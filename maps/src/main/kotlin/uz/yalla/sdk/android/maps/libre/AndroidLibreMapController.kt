package uz.yalla.sdk.android.maps.libre

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition as LibreCameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol as LibreSymbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point as GeoJsonPoint
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
import uz.yalla.maps.config.MapConstants
import uz.yalla.sdk.android.maps.common.MarkerIconLoader
import uz.yalla.sdk.android.maps.common.toPaddingPx

internal class AndroidLibreMapController(
    private val applicationContext: Context,
    initialStyleUrl: String
) : MapController, AndroidMapController {

    init {
        MapLibre.getInstance(applicationContext)
    }

    private var styleUrl: String = initialStyleUrl

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
    private var libreMap: MapLibreMap? = null
    private var libreStyle: Style? = null
    private var symbolManager: SymbolManager? = null
    private var lifecycleObserver: LifecycleEventObserver? = null
    private var attachedLifecycle: Lifecycle? = null
    private var closed = false
    private var lastEmittedCamera: CameraPosition? = null

    private var pendingMarkers: List<MapMarker> = emptyList()
    private var pendingRoutes: List<MapRoute> = emptyList()
    private var pendingPadding: PaddingValues = PaddingValues()
    private var userInitiatedMove = false
    private var warnedCirclesUnsupported = false
    private var lockedTarget: GeoPoint? = null
    private var lockedZoom: Float? = null

    private val renderedSymbols = HashMap<String, LibreSymbol>()
    private val routeSources = HashMap<String, GeoJsonSource>()
    private val routeLayers = HashMap<String, LineLayer>()
    private val uploadedIconKeys = HashSet<String>()
    private val markerData = HashMap<String, MapMarker>()
    private val routeData = HashMap<String, MapRoute>()

    override fun createView(context: Context, lifecycle: Lifecycle): View {
        ensureMainThread()
        check(!closed) { "AndroidLibreMapController is closed; create a new controller." }
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
        view.getMapAsync { lm -> onMapReady(view, lm) }
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
        libreStyle?.also { style ->
            routeLayers.values.forEach { runCatching { style.removeLayer(it) } }
            routeSources.values.forEach { runCatching { style.removeSource(it) } }
        }
        symbolManager = null
        renderedSymbols.clear()
        routeLayers.clear()
        routeSources.clear()
        markerData.clear()
        routeData.clear()
        uploadedIconKeys.clear()
        mapView = null
        libreMap = null
        libreStyle = null
        _isReady.value = false
    }

    private fun detachFromLifecycle() {
        val obs = lifecycleObserver
        val lc = attachedLifecycle
        if (obs != null && lc != null) lc.removeObserver(obs)
        lifecycleObserver = null
        attachedLifecycle = null
    }

    private fun onMapReady(view: MapView, lm: MapLibreMap) {
        libreMap = lm
        lm.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
            libreStyle = style
            symbolManager = SymbolManager(view, lm, style).apply {
                iconAllowOverlap = true
                iconIgnorePlacement = true
            }
            applyPadding(pendingPadding)
            renderMarkers(pendingMarkers)
            renderRoutes(pendingRoutes)
            _isReady.value = true
        }
        lm.addOnCameraMoveStartedListener { reason ->
            userInitiatedMove = reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE
            if (userInitiatedMove) {
                lockedTarget = null
                lockedZoom = null
            }
        }
        lm.addOnMapClickListener { point ->
            scope.launch { _events.emit(MapEvent.MapTapped(GeoPoint(point.latitude, point.longitude))) }
            false
        }
        lm.addOnMapLongClickListener { point ->
            scope.launch { _events.emit(MapEvent.MapLongPressed(GeoPoint(point.latitude, point.longitude))) }
            false
        }
        symbolManager?.addClickListener { symbol ->
            val id = renderedSymbols.entries.firstOrNull { it.value === symbol }?.key
            if (id != null) scope.launch { _events.emit(MapEvent.MarkerTapped(id)) }
            false
        }
        lm.addOnCameraMoveListener {
            val pos = lm.cameraPosition.toShared(pendingPadding)
            emitCamera(pos)
            _centerPin.value = _centerPin.value.copy(
                point = pos.target,
                isMoving = true,
                isByUser = userInitiatedMove
            )
        }
        lm.addOnCameraIdleListener {
            val pos = lm.cameraPosition.toShared(pendingPadding)
            emitCamera(pos)
            _centerPin.value = CenterPinState(
                point = pos.target,
                isMoving = false,
                isByUser = userInitiatedMove
            )
            userInitiatedMove = false
        }
    }

    override suspend fun moveTo(point: GeoPoint, zoom: Float) {
        val lm = libreMap ?: return
        lm.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                LibreCameraPosition.Builder()
                    .target(point.toLatLng())
                    .zoom(zoom.clampZoom().toDouble())
                    .build()
            )
        )
    }

    override suspend fun animateTo(point: GeoPoint, zoom: Float, durationMs: Int) {
        val lm = libreMap ?: return
        val target = LibreCameraPosition.Builder()
            .target(point.toLatLng())
            .zoom(zoom.clampZoom().toDouble())
            .build()
        animateCamera(lm, CameraUpdateFactory.newCameraPosition(target), durationMs)
    }

    override suspend fun animateToWithBearing(point: GeoPoint, bearing: Float, zoom: Float, durationMs: Int) {
        val lm = libreMap ?: return
        val target = LibreCameraPosition.Builder()
            .target(point.toLatLng())
            .zoom(zoom.clampZoom().toDouble())
            .bearing(bearing.toDouble())
            .tilt(lm.cameraPosition.tilt)
            .build()
        animateCamera(lm, CameraUpdateFactory.newCameraPosition(target), durationMs)
    }

    override suspend fun fitBounds(points: List<GeoPoint>, animate: Boolean) {
        val lm = libreMap ?: return
        val valid = points.filterNot { it == GeoPoint.Zero }.distinctBy { it.lat to it.lng }
        if (valid.isEmpty()) return
        if (valid.size == 1) {
            val single = valid.first()
            val z = lm.cameraPosition.zoom.toFloat().clampZoom()
            if (animate) animateTo(single, z, MapController.ANIMATION_DURATION) else moveTo(single, z)
            return
        }
        val bounds = LatLngBounds.fromLatLngs(valid.map { it.toLatLng() })
        val px = pendingPadding.toPaddingPx(applicationContext)
        val baseMargin = (24 * applicationContext.resources.displayMetrics.density).toInt()
        val update = CameraUpdateFactory.newLatLngBounds(
            bounds,
            px.left + baseMargin,
            px.top + baseMargin,
            px.right + baseMargin,
            px.bottom + baseMargin
        )
        if (animate) animateCamera(lm, update, MapController.ANIMATION_DURATION) else lm.moveCamera(update)
    }

    override suspend fun zoomIn() {
        val lm = libreMap ?: return
        animateCamera(lm, CameraUpdateFactory.zoomIn(), MapController.ANIMATION_DURATION)
    }

    override suspend fun zoomOut() {
        val lm = libreMap ?: return
        animateCamera(lm, CameraUpdateFactory.zoomOut(), MapController.ANIMATION_DURATION)
    }

    override suspend fun setZoom(zoom: Float) {
        val lm = libreMap ?: return
        animateCamera(lm, CameraUpdateFactory.zoomTo(zoom.clampZoom().toDouble()), MapController.ANIMATION_DURATION)
    }

    override suspend fun setStyle(style: MapStyle, isDark: Boolean) {
        val lm = libreMap ?: return
        val newUrl: String? = when (style) {
            is MapStyle.Url -> if (isDark) style.darkUrl else style.lightUrl
            is MapStyle.InlineJson -> null
            MapStyle.PlatformDefault -> null
        }
        if (newUrl != null && newUrl != styleUrl) {
            styleUrl = newUrl
            suspendCoroutine<Unit> { cont ->
                lm.setStyle(Style.Builder().fromUri(newUrl)) { style ->
                    libreStyle = style
                    val mv = mapView
                    if (mv != null) {
                        symbolManager = SymbolManager(mv, lm, style).apply {
                            iconAllowOverlap = true
                            iconIgnorePlacement = true
                        }
                    }
                    routeSources.clear()
                    routeLayers.clear()
                    renderedSymbols.clear()
                    uploadedIconKeys.clear()
                    val cachedMarkers = pendingMarkers
                    val cachedRoutes = pendingRoutes
                    markerData.clear()
                    routeData.clear()
                    renderMarkers(cachedMarkers)
                    renderRoutes(cachedRoutes)
                    kotlin.runCatching { cont.resume(Unit) }
                }
            }
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
        if (symbolManager != null) renderMarkers(markers)
    }

    override fun setRoutes(routes: List<MapRoute>) {
        ensureMainThread()
        pendingRoutes = routes
        if (libreStyle != null) renderRoutes(routes)
    }

    override fun setCircles(circles: List<MapCircle>) {
        ensureMainThread()
        if (circles.isNotEmpty() && !warnedCirclesUnsupported) {
            warnedCirclesUnsupported = true
            android.util.Log.w(
                "YallaMaps",
                "MapLibre provider does not render MapCircle. Switch to Google or wait for polygon-approximated circles. MapCapabilities.LIBRE.supportsCircles = false."
            )
        }
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
        circles = emptyList(),
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
        val lm = libreMap ?: return
        val target = lockedTarget ?: return
        val zoom = (lockedZoom ?: lm.cameraPosition.zoom.toFloat()).clampZoom().toDouble()
        scope.launch {
            animateCamera(
                lm,
                CameraUpdateFactory.newCameraPosition(
                    LibreCameraPosition.Builder().target(target.toLatLng()).zoom(zoom).build()
                ),
                250
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun applyPadding(padding: PaddingValues) {
        val lm = libreMap ?: return
        val px = padding.toPaddingPx(applicationContext)
        lm.setPadding(px.left, px.top, px.right, px.bottom)
    }

    private fun emitCamera(next: CameraPosition) {
        val prev = lastEmittedCamera
        if (prev != null && cameraEpsilonEqual(prev, next)) return
        lastEmittedCamera = next
        _cameraPosition.value = next
    }

    private fun renderMarkers(markers: List<MapMarker>) {
        val sm = symbolManager ?: return
        val style = libreMap?.style ?: return
        val incoming = markers.associateBy { it.id }
        val toRemove = renderedSymbols.keys - incoming.keys
        toRemove.forEach { id ->
            renderedSymbols.remove(id)?.let { sm.delete(it) }
            markerData.remove(id)
        }
        incoming.forEach { (id, marker) ->
            val previous = markerData[id]
            val existing = renderedSymbols[id]
            val iconImageId = marker.icon?.let { icon ->
                val key = iconImageKey(icon)
                if (key !in uploadedIconKeys) {
                    MarkerIconLoader.loadBitmap(applicationContext, icon)?.let { bitmap ->
                        style.addImage(key, bitmap)
                        uploadedIconKeys.add(key)
                    }
                }
                key
            }
            if (existing == null) {
                val options = SymbolOptions()
                    .withLatLng(marker.point.toLatLng())
                    .withIconRotate(marker.rotation)
                    .withIconAnchor(marker.anchor.toLibreAnchor())
                iconImageId?.let { options.withIconImage(it) }
                sm.create(options)?.let { renderedSymbols[id] = it }
            } else {
                if (previous?.point != marker.point) existing.latLng = marker.point.toLatLng()
                if (previous?.rotation != marker.rotation) existing.iconRotate = marker.rotation
                if (previous?.anchor != marker.anchor) existing.iconAnchor = marker.anchor.toLibreAnchor()
                if (iconImageId != null && previous?.icon != marker.icon) existing.iconImage = iconImageId
                sm.update(existing)
            }
            markerData[id] = marker
        }
    }

    private fun renderRoutes(routes: List<MapRoute>) {
        val style = libreStyle ?: return
        val incoming = routes.associateBy { it.id }
        val toRemove = routeData.keys - incoming.keys
        toRemove.forEach { id ->
            routeLayers.remove(id)?.let { runCatching { style.removeLayer(it) } }
            routeSources.remove(id)?.let { runCatching { style.removeSource(it) } }
            routeData.remove(id)
        }
        incoming.forEach { (id, route) ->
            val previous = routeData[id]
            val feature = Feature.fromGeometry(
                LineString.fromLngLats(route.points.map { GeoJsonPoint.fromLngLat(it.lng, it.lat) })
            )
            val existingSource = routeSources[id]
            val existingLayer = routeLayers[id]
            if (existingSource == null || existingLayer == null) {
                val sourceId = "yalla-route-src-$id"
                val layerId = "yalla-route-lyr-$id"
                val source = GeoJsonSource(sourceId, feature)
                style.addSource(source)
                val layer = LineLayer(layerId, sourceId).withProperties(
                    PropertyFactory.lineCap("round"),
                    PropertyFactory.lineJoin("round"),
                    PropertyFactory.lineColor(route.colorArgb.toArgbHex()),
                    PropertyFactory.lineWidth(route.widthDp)
                )
                style.addLayer(layer)
                routeSources[id] = source
                routeLayers[id] = layer
            } else {
                if (previous?.points != route.points) existingSource.setGeoJson(feature)
                if (previous?.colorArgb != route.colorArgb) {
                    existingLayer.setProperties(PropertyFactory.lineColor(route.colorArgb.toArgbHex()))
                }
                if (previous?.widthDp != route.widthDp) {
                    existingLayer.setProperties(PropertyFactory.lineWidth(route.widthDp))
                }
            }
            routeData[id] = route
        }
    }

    private suspend fun animateCamera(
        lm: MapLibreMap,
        update: org.maplibre.android.camera.CameraUpdate,
        durationMs: Int
    ) {
        suspendCoroutine<Unit> { cont ->
            val callback = object : MapLibreMap.CancelableCallback {
                override fun onFinish() { cont.resume(Unit) }
                override fun onCancel() { cont.resume(Unit) }
            }
            lm.animateCamera(update, durationMs, callback)
        }
    }

    private fun Float.clampZoom(): Float =
        coerceIn(MapConstants.ZOOM_MIN.toFloat(), MapConstants.ZOOM_MAX.toFloat())

    private fun GeoPoint.toLatLng(): LatLng = LatLng(lat, lng)

    private fun LibreCameraPosition.toShared(padding: PaddingValues): CameraPosition = CameraPosition(
        target = GeoPoint(target!!.latitude, target!!.longitude),
        zoom = zoom.toFloat(),
        bearing = bearing.toFloat(),
        tilt = tilt.toFloat(),
        padding = padding
    )

    private fun Int.toArgbHex(): String = String.format("#%08X", this)

    private fun uz.yalla.maps.api.model.Anchor.toLibreAnchor(): String = when {
        y >= 0.9f && x in 0.4f..0.6f -> "bottom"
        y <= 0.1f && x in 0.4f..0.6f -> "top"
        x <= 0.1f -> "left"
        x >= 0.9f -> "right"
        else -> "center"
    }

    private fun iconImageKey(icon: uz.yalla.maps.api.model.MapMarkerIcon): String = when (icon) {
        is uz.yalla.maps.api.model.MapMarkerIcon.Resource -> "yalla-icon-res-${icon.name}"
        is uz.yalla.maps.api.model.MapMarkerIcon.Bytes ->
            "yalla-icon-bytes-${icon.data.contentHashCode().toUInt().toString(16)}"
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
