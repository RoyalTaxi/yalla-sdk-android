package uz.yalla.sdk.android.maps.libre

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.compose.foundation.layout.PaddingValues
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
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
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition as LibreCameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol as LibreSymbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
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
import uz.yalla.maps.api.model.RoutePattern
import uz.yalla.maps.config.MapConstants
import uz.yalla.maps.util.shortestHeadingPath
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
    private var pendingCircles: List<MapCircle> = emptyList()
    private var pendingPadding: PaddingValues = PaddingValues()
    private var pendingStyle: MapStyle? = null
    private var pendingIsDark = false
    private var interactionEnabled = true
    private var userInitiatedMove = false
    private var lockedTarget: GeoPoint? = null
    private var lockedZoom: Float? = null
    private var pendingFit: PendingFit? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var animationInFlight = false
    private var animationGeneration = 0
    private var cameraMoving = false
    private var paddingDirty = false
    private var userLocation: GeoPoint? = null
    private var userLocationEnabled = true
    private var userLocationSource: GeoJsonSource? = null
    private var userLocationCircleLayer: CircleLayer? = null
    private var userLocationDotLayer: SymbolLayer? = null

    private val renderedSymbols = HashMap<String, LibreSymbol>()

    private val markerAnimators = HashMap<String, ValueAnimator>()
    private val routeSources = HashMap<String, GeoJsonSource>()
    private val routeLayers = HashMap<String, LineLayer>()
    private val circleSources = HashMap<String, GeoJsonSource>()
    private val circleLayers = HashMap<String, CircleLayer>()
    private val uploadedIconKeys = HashSet<String>()
    private val markerData = HashMap<String, MapMarker>()
    private val routeData = HashMap<String, MapRoute>()
    private val circleData = HashMap<String, MapCircle>()

    override fun createView(context: Context, lifecycle: Lifecycle): View {
        ensureMainThread()
        check(!closed) { "AndroidLibreMapController is closed; create a new controller." }
        detachFromLifecycle()
        val options = MapLibreMapOptions.createFromAttributes(context).textureMode(true)
        val view = MapView(context, options).apply { onCreate(Bundle()) }
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
        view.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (v.width > 0 && v.height > 0 && _isReady.value) flushPendingFit()
        }
        view.getMapAsync { lm -> onMapReady(view, lm) }
        return view
    }

    private fun flushPendingFit() {
        val fit = pendingFit ?: return
        pendingFit = null
        scope.launch { fitBounds(fit.points, fit.animate, fit.padding) }
    }

    override fun detach() {
        ensureMainThread()
        animationInFlight = false
        cameraMoving = false
        paddingDirty = false
        detachFromLifecycle()
        mapView?.also { mv ->
            mv.onPause()
            mv.onStop()
            mv.onDestroy()
        }
        libreStyle?.also { style ->
            routeLayers.values.forEach { runCatching { style.removeLayer(it) } }
            routeSources.values.forEach { runCatching { style.removeSource(it) } }
            circleLayers.values.forEach { runCatching { style.removeLayer(it) } }
            circleSources.values.forEach { runCatching { style.removeSource(it) } }
            userLocationDotLayer?.let { runCatching { style.removeLayer(it) } }
            userLocationCircleLayer?.let { runCatching { style.removeLayer(it) } }
            userLocationSource?.let { runCatching { style.removeSource(it) } }
        }
        userLocationDotLayer = null
        userLocationCircleLayer = null
        userLocationSource = null
        symbolManager = null
        markerAnimators.values.forEach { it.cancel() }
        markerAnimators.clear()
        renderedSymbols.clear()
        routeLayers.clear()
        routeSources.clear()
        circleLayers.clear()
        circleSources.clear()
        markerData.clear()
        routeData.clear()
        circleData.clear()
        uploadedIconKeys.clear()
        pendingFit = null
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
        lm.uiSettings.isAttributionEnabled = false
        lm.uiSettings.isLogoEnabled = false
        lm.uiSettings.isCompassEnabled = false
        lm.uiSettings.isRotateGesturesEnabled = false
        lm.uiSettings.isTiltGesturesEnabled = false
        lm.setMinZoomPreference(MapConstants.ZOOM_MIN)
        lm.setMaxZoomPreference(MapConstants.ZOOM_MAX)
        applyInteractionEnabled()
        pendingStyle?.resolveUrl(pendingIsDark)?.let { styleUrl = it }
        lm.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
            libreStyle = style
            applyPadding(pendingPadding)
            attachSymbolManager(view, lm, style)
            renderMarkers(pendingMarkers)
            renderRoutes(pendingRoutes)
            renderCircles(pendingCircles)
            renderUserLocation()
            _isReady.value = true
            if (view.width > 0 && view.height > 0) flushPendingFit()
        }
        lm.addOnCameraMoveStartedListener { reason ->
            cameraMoving = true
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
            cameraMoving = false
            if (paddingDirty) {
                paddingDirty = false
                applyPadding(pendingPadding)
            }
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

    private fun attachSymbolManager(view: MapView, lm: MapLibreMap, style: Style) {
        symbolManager = SymbolManager(view, lm, style).apply {
            iconAllowOverlap = true
            iconIgnorePlacement = true
            addClickListener { symbol ->
                val id = renderedSymbols.entries.firstOrNull { it.value === symbol }?.key
                if (id != null) scope.launch { _events.emit(MapEvent.MarkerTapped(id)) }
                false
            }
        }
    }

    override suspend fun moveTo(point: GeoPoint, zoom: Float) {
        pendingFit = null
        val lm = libreMap ?: return
        val basePx = pendingPadding.toPaddingPx(applicationContext)
        lm.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                LibreCameraPosition.Builder()
                    .target(point.toLatLng())
                    .zoom(zoom.clampZoom().toDouble())
                    .padding(basePx.left.toDouble(), basePx.top.toDouble(), basePx.right.toDouble(), basePx.bottom.toDouble())
                    .build()
            )
        )
    }

    override suspend fun animateTo(point: GeoPoint, zoom: Float, durationMs: Int) {
        pendingFit = null
        val lm = libreMap ?: return
        val basePx = pendingPadding.toPaddingPx(applicationContext)
        val target = LibreCameraPosition.Builder()
            .target(point.toLatLng())
            .zoom(zoom.clampZoom().toDouble())
            .padding(basePx.left.toDouble(), basePx.top.toDouble(), basePx.right.toDouble(), basePx.bottom.toDouble())
            .build()
        animateCamera(lm, CameraUpdateFactory.newCameraPosition(target), durationMs)
    }

    override suspend fun animateToWithBearing(point: GeoPoint, bearing: Float, zoom: Float, durationMs: Int) {
        pendingFit = null
        val lm = libreMap ?: return
        val basePx = pendingPadding.toPaddingPx(applicationContext)
        val target = LibreCameraPosition.Builder()
            .target(point.toLatLng())
            .zoom(zoom.clampZoom().toDouble())
            .bearing(bearing.toDouble())
            .tilt(lm.cameraPosition.tilt)
            .padding(basePx.left.toDouble(), basePx.top.toDouble(), basePx.right.toDouble(), basePx.bottom.toDouble())
            .build()
        animateCamera(lm, CameraUpdateFactory.newCameraPosition(target), durationMs)
    }

    override suspend fun fitBounds(points: List<GeoPoint>, animate: Boolean, padding: PaddingValues?) {
        pendingFit = null
        val valid = points.filterNot { it == GeoPoint.Zero }.distinctBy { it.lat to it.lng }
        if (valid.isEmpty()) return
        val lm = libreMap
        val view = mapView
        if (lm == null || view == null || view.width == 0 || view.height == 0) {
            pendingFit = PendingFit(points, animate, padding)
            return
        }
        if (valid.size == 1) {
            val single = valid.first()
            val z = lm.cameraPosition.zoom.toFloat().clampZoom()
            if (animate) animateTo(single, z, MapController.ANIMATION_DURATION) else moveTo(single, z)
            return
        }
        val bounds = LatLngBounds.fromLatLngs(valid.map { it.toLatLng() })
        val basePx = pendingPadding.toPaddingPx(applicationContext)
        val marginPx = (padding ?: PaddingValues(MapConstants.DEFAULT_PADDING)).toPaddingPx(applicationContext)
        val margin = maxOf(marginPx.left, marginPx.top, marginPx.right, marginPx.bottom)
        val fitPadding = intArrayOf(basePx.left + margin, basePx.top + margin, basePx.right + margin, basePx.bottom + margin)
        val fitted = lm.getCameraForLatLngBounds(bounds, fitPadding, 0.0, 0.0)
        if (fitted == null) {
            pendingFit = PendingFit(points, animate, padding)
            return
        }
        val target = LibreCameraPosition.Builder()
            .target(fitted.target)
            .zoom(fitted.zoom.toFloat().clampZoom().toDouble())
            .bearing(0.0)
            .tilt(0.0)
            .padding(fitPadding[0].toDouble(), fitPadding[1].toDouble(), fitPadding[2].toDouble(), fitPadding[3].toDouble())
            .build()
        val update = CameraUpdateFactory.newCameraPosition(target)
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
        pendingStyle = style
        pendingIsDark = isDark
        val lm = libreMap ?: return
        val newUrl = style.resolveUrl(isDark)
        if (newUrl != null && newUrl != styleUrl) {
            styleUrl = newUrl
            suspendCoroutine<Unit> { cont ->
                lm.setStyle(Style.Builder().fromUri(newUrl)) { style ->
                    libreStyle = style
                    val mv = mapView
                    if (mv != null) attachSymbolManager(mv, lm, style)
                    markerAnimators.values.forEach { it.cancel() }
                    markerAnimators.clear()
                    routeSources.clear()
                    routeLayers.clear()
                    circleSources.clear()
                    circleLayers.clear()
                    renderedSymbols.clear()
                    uploadedIconKeys.clear()
                    userLocationSource = null
                    userLocationCircleLayer = null
                    userLocationDotLayer = null
                    val cachedMarkers = pendingMarkers
                    val cachedRoutes = pendingRoutes
                    val cachedCircles = pendingCircles
                    markerData.clear()
                    routeData.clear()
                    circleData.clear()
                    renderMarkers(cachedMarkers)
                    renderRoutes(cachedRoutes)
                    renderCircles(cachedCircles)
                    renderUserLocation()
                    kotlin.runCatching { cont.resume(Unit) }
                }
            }
        }
    }

    override fun setDesiredPadding(padding: PaddingValues) {
        ensureMainThread()
        if (padding == pendingPadding && libreMap != null) return
        pendingPadding = padding
        applyPadding(padding)
    }

    override fun setInteractionEnabled(enabled: Boolean) {
        ensureMainThread()
        interactionEnabled = enabled
        applyInteractionEnabled()
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
        pendingCircles = circles
        if (libreStyle != null) renderCircles(circles)
    }

    override fun setUserLocation(point: GeoPoint?) {
        ensureMainThread()
        userLocation = point
        if (libreStyle != null) renderUserLocation()
    }

    override fun setUserLocationEnabled(enabled: Boolean) {
        ensureMainThread()
        userLocationEnabled = enabled
        if (libreStyle != null) renderUserLocation()
    }

    private fun renderCircles(circles: List<MapCircle>) {
        val style = libreMap?.style ?: return
        val incoming = circles.associateBy { it.id }
        val toRemove = circleData.keys - incoming.keys
        toRemove.forEach { id ->
            circleLayers.remove(id)?.let { runCatching { style.removeLayer(it) } }
            circleSources.remove(id)?.let { runCatching { style.removeSource(it) } }
            circleData.remove(id)
        }
        incoming.forEach { (id, circle) ->
            val previous = circleData[id]
            val feature = Feature.fromGeometry(
                GeoJsonPoint.fromLngLat(circle.center.lng, circle.center.lat)
            )
            val existingSource = circleSources[id]
            val existingLayer = circleLayers[id]
            if (existingSource == null || existingLayer == null) {
                val sourceId = "yalla-circle-src-$id"
                val layerId = "yalla-circle-lyr-$id"
                val source = GeoJsonSource(sourceId, feature)
                style.addSource(source)
                val layer = CircleLayer(layerId, sourceId).withProperties(
                    PropertyFactory.circleRadius(circleRadiusExpression(circle.radiusMeters, circle.center.lat)),
                    PropertyFactory.circleColor(circle.fillColorArgb),
                    PropertyFactory.circleStrokeColor(circle.strokeColorArgb),
                    PropertyFactory.circleStrokeWidth(circle.strokeWidthDp)
                )
                val anchorLayerId = symbolManager?.layerId
                if (anchorLayerId != null) {
                    runCatching { style.addLayerBelow(layer, anchorLayerId) }
                        .onFailure { style.addLayer(layer) }
                } else {
                    style.addLayer(layer)
                }
                circleSources[id] = source
                circleLayers[id] = layer
            } else {
                if (previous?.center != circle.center) existingSource.setGeoJson(feature)
                if (previous?.center != circle.center || previous?.radiusMeters != circle.radiusMeters) {
                    existingLayer.setProperties(PropertyFactory.circleRadius(circleRadiusExpression(circle.radiusMeters, circle.center.lat)))
                }
                if (previous?.fillColorArgb != circle.fillColorArgb) {
                    existingLayer.setProperties(PropertyFactory.circleColor(circle.fillColorArgb))
                }
                if (previous?.strokeColorArgb != circle.strokeColorArgb) {
                    existingLayer.setProperties(PropertyFactory.circleStrokeColor(circle.strokeColorArgb))
                }
                if (previous?.strokeWidthDp != circle.strokeWidthDp) {
                    existingLayer.setProperties(PropertyFactory.circleStrokeWidth(circle.strokeWidthDp))
                }
            }
            circleData[id] = circle
        }
    }

    private fun renderUserLocation() {
        val style = libreStyle ?: return
        val point = userLocation.takeIf { userLocationEnabled }
        if (point == null) {
            userLocationDotLayer?.let { runCatching { style.removeLayer(it) } }
            userLocationCircleLayer?.let { runCatching { style.removeLayer(it) } }
            userLocationSource?.let { runCatching { style.removeSource(it) } }
            userLocationDotLayer = null
            userLocationCircleLayer = null
            userLocationSource = null
            return
        }
        if (USER_LOCATION_ICON_ID !in uploadedIconKeys) {
            style.addImage(USER_LOCATION_ICON_ID, MarkerIconLoader.loadUserLocationBitmap(applicationContext))
            uploadedIconKeys.add(USER_LOCATION_ICON_ID)
        }
        val feature = Feature.fromGeometry(GeoJsonPoint.fromLngLat(point.lng, point.lat))
        val existingSource = userLocationSource
        if (existingSource == null) {
            val source = GeoJsonSource(USER_LOCATION_SOURCE_ID, feature)
            style.addSource(source)
            val circleLayer = CircleLayer(USER_LOCATION_CIRCLE_LAYER_ID, USER_LOCATION_SOURCE_ID).withProperties(
                PropertyFactory.circleRadius(circleRadiusExpression(USER_LOCATION_ACCURACY_METERS, point.lat)),
                PropertyFactory.circleColor(USER_LOCATION_FILL_COLOR),
                PropertyFactory.circleStrokeColor(USER_LOCATION_STROKE_COLOR),
                PropertyFactory.circleStrokeWidth(1f)
            )
            val anchorLayerId = symbolManager?.layerId
            if (anchorLayerId != null) {
                runCatching { style.addLayerBelow(circleLayer, anchorLayerId) }
                    .onFailure { style.addLayer(circleLayer) }
            } else {
                style.addLayer(circleLayer)
            }
            val dotLayer = SymbolLayer(USER_LOCATION_DOT_LAYER_ID, USER_LOCATION_SOURCE_ID).withProperties(
                PropertyFactory.iconImage(USER_LOCATION_ICON_ID),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
            style.addLayer(dotLayer)
            userLocationSource = source
            userLocationCircleLayer = circleLayer
            userLocationDotLayer = dotLayer
        } else {
            existingSource.setGeoJson(feature)
            userLocationCircleLayer?.setProperties(PropertyFactory.circleRadius(circleRadiusExpression(USER_LOCATION_ACCURACY_METERS, point.lat)))
        }
    }

    private fun circleRadiusExpression(radiusMeters: Double, lat: Double): Expression {
        val radiusAtZoomZero = (radiusMeters / (BASE_METERS_PER_PIXEL * cos(lat * PI / 180.0))).toFloat()
        return Expression.interpolate(
            Expression.exponential(2),
            Expression.zoom(),
            Expression.stop(0, radiusAtZoomZero),
            Expression.stop(22, radiusAtZoomZero * 2f.pow(22))
        )
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

    private fun applyPadding(padding: PaddingValues) {
        val lm = libreMap ?: return
        if (animationInFlight || cameraMoving) {
            paddingDirty = true
            return
        }
        val px = padding.toPaddingPx(applicationContext)
        lm.moveCamera(
            CameraUpdateFactory.paddingTo(
                px.left.toDouble(),
                px.top.toDouble(),
                px.right.toDouble(),
                px.bottom.toDouble()
            )
        )
    }

    private fun applyInteractionEnabled() {
        val lm = libreMap ?: return
        lm.uiSettings.isScrollGesturesEnabled = interactionEnabled
        lm.uiSettings.isZoomGesturesEnabled = interactionEnabled
        lm.uiSettings.isDoubleTapGesturesEnabled = interactionEnabled
        lm.uiSettings.isQuickZoomGesturesEnabled = interactionEnabled
        lm.uiSettings.isRotateGesturesEnabled = false
        lm.uiSettings.isTiltGesturesEnabled = false
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
            markerAnimators.remove(id)?.cancel()
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
                        bitmap.density = applicationContext.resources.displayMetrics.densityDpi
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
                    .withSymbolSortKey(marker.zIndex)
                iconImageId?.let { options.withIconImage(it) }
                sm.create(options)?.let { renderedSymbols[id] = it }
            } else {
                val moved = previous?.point != marker.point || previous?.rotation != marker.rotation
                if (moved && marker.flat) {
                    animateSymbol(id, existing, marker)
                } else if (moved) {
                    markerAnimators.remove(id)?.cancel()
                    existing.latLng = marker.point.toLatLng()
                    existing.iconRotate = marker.rotation
                }
                if (previous?.anchor != marker.anchor) existing.iconAnchor = marker.anchor.toLibreAnchor()
                if (previous?.zIndex != marker.zIndex) existing.symbolSortKey = marker.zIndex
                if (iconImageId != null && previous?.icon != marker.icon) existing.iconImage = iconImageId
                sm.update(existing)
            }
            markerData[id] = marker
        }
    }

    private fun animateSymbol(id: String, symbol: LibreSymbol, target: MapMarker) {
        markerAnimators.remove(id)?.cancel()
        val startPosition = symbol.latLng
        val endPosition = target.point.toLatLng()
        val startRotation = symbol.iconRotate
        val endRotation = shortestHeadingPath(startRotation, target.rotation)
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = MARKER_ANIMATION_MS
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val sm = symbolManager ?: return@addUpdateListener
                val fraction = animation.animatedValue as Float
                symbol.latLng = LatLng(
                    startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction,
                    startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
                )
                symbol.iconRotate = startRotation + (endRotation - startRotation) * fraction
                sm.update(symbol)
            }
        }
        markerAnimators[id] = animator
        animator.start()
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
                    PropertyFactory.lineColor(route.colorArgb),
                    PropertyFactory.lineWidth(route.widthDp)
                )
                route.pattern.toLibreDashArray(route.widthDp)?.let { layer.setProperties(PropertyFactory.lineDasharray(it)) }
                val anchorLayerId = circleLayers.values.firstOrNull()?.id ?: symbolManager?.layerId
                if (anchorLayerId != null) {
                    runCatching { style.addLayerBelow(layer, anchorLayerId) }
                        .onFailure { style.addLayer(layer) }
                } else {
                    style.addLayer(layer)
                }
                routeSources[id] = source
                routeLayers[id] = layer
            } else {
                if (previous?.points != route.points) existingSource.setGeoJson(feature)
                if (previous?.colorArgb != route.colorArgb) {
                    existingLayer.setProperties(PropertyFactory.lineColor(route.colorArgb))
                }
                if (previous?.widthDp != route.widthDp) {
                    existingLayer.setProperties(PropertyFactory.lineWidth(route.widthDp))
                }
                if (previous?.pattern != route.pattern || previous?.widthDp != route.widthDp) {
                    existingLayer.setProperties(PropertyFactory.lineDasharray(route.pattern.toLibreDashArray(route.widthDp)))
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
        suspendCancellableCoroutine<Unit> { cont ->
            val generation = ++animationGeneration
            cont.invokeOnCancellation { mainHandler.post { lm.cancelTransitions() } }
            animationInFlight = true
            lm.easeCamera(update, durationMs, object : MapLibreMap.CancelableCallback {
                override fun onFinish() { onAnimationEnd(generation); if (cont.isActive) cont.resume(Unit) }
                override fun onCancel() { onAnimationEnd(generation); if (cont.isActive) cont.resume(Unit) }
            })
        }
    }

    private fun onAnimationEnd(generation: Int) {
        if (generation != animationGeneration) return
        animationInFlight = false
        if (paddingDirty) {
            paddingDirty = false
            applyPadding(pendingPadding)
        }
    }

    private fun Float.clampZoom(): Float =
        coerceIn(MapConstants.ZOOM_MIN.toFloat(), MapConstants.ZOOM_MAX.toFloat())

    private fun GeoPoint.toLatLng(): LatLng = LatLng(lat, lng)

    private fun MapStyle.resolveUrl(isDark: Boolean): String? = when (this) {
        is MapStyle.Url -> if (isDark) darkUrl else lightUrl
        is MapStyle.InlineJson -> null
        MapStyle.PlatformDefault -> null
    }

    private fun LibreCameraPosition.toShared(padding: PaddingValues): CameraPosition = CameraPosition(
        target = GeoPoint(target!!.latitude, target!!.longitude),
        zoom = zoom.toFloat(),
        bearing = bearing.toFloat(),
        tilt = tilt.toFloat(),
        padding = padding
    )

    private fun uz.yalla.maps.api.model.Anchor.toLibreAnchor(): String = when {
        y >= 0.9f && x in 0.4f..0.6f -> "bottom"
        y <= 0.1f && x in 0.4f..0.6f -> "top"
        x <= 0.1f -> "left"
        x >= 0.9f -> "right"
        else -> "center"
    }

    private fun RoutePattern.toLibreDashArray(widthDp: Float): Array<Float>? = when (this) {
        RoutePattern.SOLID -> null
        RoutePattern.DASHED -> arrayOf(30f / widthDp, 20f / widthDp)
        RoutePattern.DOTTED -> arrayOf(0f, 20f / widthDp)
    }

    private fun iconImageKey(icon: uz.yalla.maps.api.model.MapMarkerIcon): String = when (icon) {
        is uz.yalla.maps.api.model.MapMarkerIcon.Resource -> "yalla-icon-res-${icon.name}"
        is uz.yalla.maps.api.model.MapMarkerIcon.Bytes ->
            "yalla-icon-bytes-${icon.data.contentHashCode().toUInt().toString(16)}"
        is uz.yalla.maps.api.model.MapMarkerIcon.Pin ->
            "yalla-icon-pin-${icon.colorArgb.toUInt().toString(16)}-${icon.label.orEmpty().hashCode().toUInt().toString(16)}"
        is uz.yalla.maps.api.model.MapMarkerIcon.Dot ->
            "yalla-icon-dot-${icon.fillColorArgb.toUInt().toString(16)}-${icon.strokeColorArgb.toUInt().toString(16)}-${icon.diameterDp}-${icon.strokeWidthDp}"
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

    private companion object {
        const val MARKER_ANIMATION_MS = 1000L
        const val USER_LOCATION_ACCURACY_METERS = 50.0
        const val USER_LOCATION_FILL_COLOR = 0x33562DF8
        const val USER_LOCATION_STROKE_COLOR = 0x66562DF8
        const val USER_LOCATION_SOURCE_ID = "yalla-user-location-src"
        const val USER_LOCATION_CIRCLE_LAYER_ID = "yalla-user-location-circle"
        const val USER_LOCATION_DOT_LAYER_ID = "yalla-user-location-dot"
        const val USER_LOCATION_ICON_ID = "yalla-icon-user-location"
        const val BASE_METERS_PER_PIXEL = 156543.03392
    }

    private data class PendingFit(
        val points: List<GeoPoint>,
        val animate: Boolean,
        val padding: PaddingValues?
    )
}
