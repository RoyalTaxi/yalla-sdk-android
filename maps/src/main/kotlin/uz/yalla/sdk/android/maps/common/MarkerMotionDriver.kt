package uz.yalla.sdk.android.maps.common

import android.animation.ValueAnimator
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import uz.yalla.core.geo.GeoPoint
import uz.yalla.maps.motion.DriverMotionModel

internal class MarkerMotionDriver(
    private val write: (id: String, point: GeoPoint, bearing: Float) -> Unit
) {

    private val models = HashMap<String, DriverMotionModel>()
    private val lastEmitted = HashMap<String, Pose>()
    private var animator: ValueAnimator? = null

    private data class Pose(val lat: Double, val lng: Double, val bearing: Float)

    fun push(id: String, point: GeoPoint, routeHeading: Float?, serverHeading: Float, atMillis: Long) {
        val model = models.getOrPut(id) { DriverMotionModel() }
        model.push(point, routeHeading, serverHeading, atMillis)
        ensureRunning()
    }

    fun has(id: String): Boolean = models.containsKey(id)

    fun remove(id: String) {
        models.remove(id)
        lastEmitted.remove(id)
        if (models.isEmpty()) stop()
    }

    fun clear() {
        models.clear()
        lastEmitted.clear()
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
            if (last != null &&
                MapMath.posesClose(last.lat, last.lng, last.bearing, pose.point.lat, pose.point.lng, pose.bearing)
            ) {
                continue
            }
            lastEmitted[id] = Pose(pose.point.lat, pose.point.lng, pose.bearing)
            write(id, pose.point, pose.bearing)
        }
    }

    private companion object {
        const val FRAME_LOOP_MS = 1_000L
    }
}
