package uz.yalla.sdk.android.maps.common

import android.animation.ValueAnimator
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import uz.yalla.core.geo.GeoPoint
import uz.yalla.maps.config.RouteFollowingConfig
import uz.yalla.maps.motion.DriverMotionModel
import uz.yalla.maps.motion.RouteConnector

/**
 * Per-frame driver for flat (driver) markers. A Humble Object: it owns no geometry, it only samples
 * the SDK's [DriverMotionModel] each animation frame and writes the resulting pose, the model's
 * already-trimmed [remaining route][DriverMotionModel.remainingRoute] state, and the model's
 * [connector][DriverMotionModel.connector] honesty line to the renderer.
 *
 * Route-following is gated behind the SDK feature flag: [routeFollowingEnabled] is forwarded to every
 * model this driver creates. With the flag off (the production default) [setRoute] is a no-op in the
 * model and the driver is a pure chord interpolator; the route-eating / off-route surface is dormant.
 *
 * All trimming, projection, snapping, hysteresis, cooldown and throttling live in the model behind
 * SDK config ([RouteFollowingConfig]) — this class contains no geo math.
 *
 * @param write writes the sampled pose (position + bearing) for a marker id.
 * @param writeRoute draws the remaining-route polyline the model hands back, verbatim.
 * @param writeConnector draws (or clears, on `null`) the raw-GPS → snapped honesty line for a marker.
 * @param onOffRouteSignal invoked once per ON_ROUTE→OFF_ROUTE crossing the model latches, so the
 *   client (never the SDK — ADR 0002) can refetch the route and re-seed via [setRoute].
 * @param routeFollowingEnabled SDK feature flag; forwarded to every [DriverMotionModel] created.
 */
internal class MarkerMotionDriver(
    private val write: (id: String, point: GeoPoint, bearing: Float) -> Unit,
    private val writeRoute: (id: String, points: List<GeoPoint>) -> Unit = { _, _ -> },
    private val writeConnector: (id: String, connector: RouteConnector?) -> Unit = { _, _ -> },
    private val onOffRouteSignal: (id: String) -> Unit = { _ -> },
    private val routeFollowingEnabled: Boolean = false,
    private val routeConfig: RouteFollowingConfig = RouteFollowingConfig()
) {

    private val models = HashMap<String, DriverMotionModel>()
    private val lastEmitted = HashMap<String, Pose>()
    private val lastRoute = HashMap<String, List<GeoPoint>>()
    private val lastConnector = HashMap<String, RouteConnector?>()
    private val followedRouteIds = HashMap<String, String>()
    private var animator: ValueAnimator? = null

    private data class Pose(val lat: Double, val lng: Double, val bearing: Float)

    fun push(id: String, point: GeoPoint, routeHeading: Float?, serverHeading: Float, atMillis: Long) {
        val model = models.getOrPut(id) {
            DriverMotionModel(
                routeFollowingEnabled = routeFollowingEnabled,
                routeConfig = routeConfig
            )
        }
        model.push(point, routeHeading, serverHeading, atMillis)
        ensureRunning()
    }

    /**
     * Sets (or clears) the route marker [id] follows. With the feature flag off this is inert (the
     * model's [setRoute] returns immediately). [routeId] identifies the rendered polyline the driver
     * trims as the car advances.
     */
    fun setRoute(id: String, routeId: String, route: List<GeoPoint>?) {
        val model = models[id] ?: return
        model.setRoute(route)
        if (route == null) {
            followedRouteIds.remove(id)
            lastRoute.remove(id)
            clearConnector(id)
        } else {
            followedRouteIds[id] = routeId
        }
    }

    fun has(id: String): Boolean = models.containsKey(id)

    fun remove(id: String) {
        models.remove(id)
        lastEmitted.remove(id)
        lastRoute.remove(id)
        lastConnector.remove(id)
        followedRouteIds.remove(id)
        if (models.isEmpty()) stop()
    }

    fun clear() {
        models.clear()
        lastEmitted.clear()
        lastRoute.clear()
        lastConnector.clear()
        followedRouteIds.clear()
        stop()
    }

    private fun ensureRunning() {
        if (animator != null || models.isEmpty()) return
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = FRAME_LOOP_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { tick() }
            start()
        }
    }

    private fun stop() {
        animator?.cancel()
        animator = null
    }

    private fun tick() {
        if (models.isEmpty()) {
            stop()
            return
        }
        val now = SystemClock.uptimeMillis()
        for ((id, model) in models) {
            val pose = model.sample(now)
            val last = lastEmitted[id]
            val poseChanged = last == null ||
                !MapMath.posesClose(last.lat, last.lng, last.bearing, pose.point.lat, pose.point.lng, pose.bearing)
            if (poseChanged) {
                lastEmitted[id] = Pose(pose.point.lat, pose.point.lng, pose.bearing)
                write(id, pose.point, pose.bearing)
            }
            if (model.consumeOffRouteSignal()) onOffRouteSignal(id)
            if (model.isFollowingRoute()) {
                emitRoute(id, model, now)
                emitConnector(id, model, now)
            } else {
                clearConnector(id)
            }
        }
    }

    /**
     * Emits the model's already-trimmed remaining-route state for [id]. The trimming, arrival cutoff
     * and projection all happened in the model; this only forwards the list and de-dups identical
     * emissions so the polyline source is not rewritten on frames where the state did not change.
     */
    private fun emitRoute(id: String, model: DriverMotionModel, now: Long) {
        val routeId = followedRouteIds[id] ?: return
        val remaining = model.remainingRoute(now)
        if (remaining.isEmpty()) return
        if (lastRoute[id] == remaining) return
        lastRoute[id] = remaining
        writeRoute(routeId, remaining)
    }

    /**
     * Emits the model's connector state (raw GPS → snapped car) for [id], de-duping identical lines.
     * `null` clears the previously drawn connector. The renderer draws the line verbatim.
     */
    private fun emitConnector(id: String, model: DriverMotionModel, now: Long) {
        setConnector(id, model.connector(now))
    }

    private fun clearConnector(id: String) {
        setConnector(id, null)
    }

    private fun setConnector(id: String, connector: RouteConnector?) {
        if (lastConnector.containsKey(id) && lastConnector[id] == connector) return
        lastConnector[id] = connector
        writeConnector(id, connector)
    }

    private companion object {
        const val FRAME_LOOP_MS = 1_000L
    }
}
