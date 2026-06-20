package uz.yalla.sdk.android.maps.common

import android.animation.ValueAnimator
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import uz.yalla.core.geo.GeoPoint
import uz.yalla.maps.motion.DriverMotionModel

/**
 * One frame driver for all animated (flat) markers on a controller — the Android counterpart of iOS
 * `MarkerMotionDriver`. A single infinite [ValueAnimator] samples every registered
 * [DriverMotionModel] each frame and writes a pose back via the per-id [write] callback **only when
 * the pose moved past the renderers' epsilon** ([MapMath.posesClose]); a parked car samples the same
 * pose every frame and is skipped, so a settled marker is not re-written 30x/second (battery/jank on
 * the long "waiting for driver" wait). The loop stops itself when no models remain.
 *
 * Replaces the previous per-marker 10s `ValueAnimator` (one animator per car, every animated car
 * re-written every frame for a fixed 10s after it stopped). "Share the math, not the shell": the
 * shared [DriverMotionModel] is the math; this is the thin Android shell, mirroring iOS.
 *
 * Threading: all methods must be called from the Android main thread (the controllers enforce this);
 * [DriverMotionModel] is single-threaded by contract.
 *
 * @param write applies a sampled pose for one marker id to the underlying native object. The driver
 *   never writes a settled (epsilon-equal) pose, so this fires only on real movement.
 */
internal class MarkerMotionDriver(
    private val write: (id: String, point: GeoPoint, bearing: Float) -> Unit
) {

    private val models = HashMap<String, DriverMotionModel>()
    private val lastEmitted = HashMap<String, Pose>()
    private var animator: ValueAnimator? = null

    private data class Pose(val lat: Double, val lng: Double, val bearing: Float)

    /** Feeds one positional fix for [id], creating the model on first sight, and starts the loop. */
    fun push(id: String, point: GeoPoint, routeHeading: Float?, serverHeading: Float, atMillis: Long) {
        val model = models.getOrPut(id) { DriverMotionModel() }
        model.push(point, routeHeading, serverHeading, atMillis)
        ensureRunning()
    }

    /** True if [id] already has a motion model (used to seed it without forcing a write). */
    fun has(id: String): Boolean = models.containsKey(id)

    /** Removes a single marker's model; stops the loop if it was the last one. */
    fun remove(id: String) {
        models.remove(id)
        lastEmitted.remove(id)
        if (models.isEmpty()) stop()
    }

    /** Drops everything and stops the loop. */
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
            // Skip the native write for a settled car (pose within the renderers' write epsilon).
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
        // The loop is infinite (stopped when no models remain); this is only the per-cycle window
        // of the driving ValueAnimator and does not bound how long a marker animates.
        const val FRAME_LOOP_MS = 1_000L
    }
}
