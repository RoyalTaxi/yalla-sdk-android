package uz.yalla.sdk.android.maps.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import uz.yalla.sdk.android.maps.model.CameraPosition
import uz.yalla.sdk.android.maps.model.LatLng
import uz.yalla.sdk.android.maps.model.LatLngBounds

/**
 * Reason the map camera started moving.
 *
 * Used by [CameraPositionState] to distinguish user gestures from programmatic animations.
 */
enum class CameraMoveStartedReason {
    /** Movement reason could not be determined. */
    UNKNOWN,

    /** Camera has not moved since state creation. */
    NO_MOVEMENT_YET,

    /** Movement was triggered by a user gesture (pan, pinch, etc.). */
    GESTURE,

    /** Movement was triggered by an API animation (e.g., `animate`). */
    API_ANIMATION,

    /** Movement was triggered by a developer animation. */
    DEVELOPER_ANIMATION
}

/**
 * Default animation duration in milliseconds for [CameraPositionState.animate].
 */
const val DEFAULT_ANIMATION_DURATION_MS: Int = 300

/**
 * Internal request type emitted by [CameraPositionState] to platform-specific handlers.
 */
internal sealed class CameraAnimationRequest {
    abstract val durationMs: Int

    /**
     * Request to animate the camera to a specific [position].
     */
    data class ToPosition(
        val position: CameraPosition,
        override val durationMs: Int
    ) : CameraAnimationRequest()

    /**
     * Request to animate the camera to fit geographic [bounds] within the viewport.
     */
    data class ToBounds(
        val bounds: LatLngBounds,
        val padding: Int,
        override val durationMs: Int
    ) : CameraAnimationRequest()
}

/**
 * Mutable state holder for the map camera position.
 *
 * Bridges the shared Kotlin layer and the platform map SDK by exposing reactive
 * [position], [isMoving], and [cameraMoveStartedReason] properties. Camera
 * changes can be applied via [animate], [animateToBounds], or [move].
 */
@Stable
class CameraPositionState(
    position: CameraPosition =
        CameraPosition(
            target = LatLng(0.0, 0.0),
            zoom = 10f
        )
) {
    /**
     * Whether the camera is currently in motion (animating or being dragged).
     */
    var isMoving: Boolean by mutableStateOf(false)
        internal set

    /**
     * Reason the most recent camera movement started.
     */
    var cameraMoveStartedReason: CameraMoveStartedReason by mutableStateOf(
        CameraMoveStartedReason.NO_MOVEMENT_YET
    )
        internal set

    /**
     * Backing Compose state for the camera position, updated by both the cross-platform
     * API and the platform-specific synchronization layer.
     */
    internal var rawPosition: CameraPosition by mutableStateOf(position)

    /**
     * Platform-specific callback installed by the actual Google Maps synchronization layer.
     *
     * When set, [position] setter delegates through this to move the platform camera
     * and update [rawPosition] atomically. When `null`, [rawPosition] is updated directly.
     */
    internal var positionUpdater: ((CameraPosition) -> Unit)? = null

    /**
     * The current camera position. Setting this triggers an immediate (non-animated) move.
     */
    var position: CameraPosition
        get() = rawPosition
        set(value) {
            positionUpdater?.invoke(value) ?: run { rawPosition = value }
        }

    /**
     * Channel for queued animation requests, consumed by the platform synchronization layer.
     */
    internal val animationRequests = MutableSharedFlow<CameraAnimationRequest>(extraBufferCapacity = 1)

    /**
     * Channel for queued instant-move requests, consumed by the platform synchronization layer.
     */
    internal val moveRequests = MutableSharedFlow<CameraPosition>(extraBufferCapacity = 1)

    /**
     * Smoothly animates the camera to the given [position].
     */
    suspend fun animate(
        position: CameraPosition,
        durationMs: Int = DEFAULT_ANIMATION_DURATION_MS
    ) {
        animationRequests.emit(CameraAnimationRequest.ToPosition(position, durationMs))
    }

    /**
     * Smoothly animates the camera to fit the given [bounds] within the viewport.
     */
    suspend fun animateToBounds(
        bounds: LatLngBounds,
        padding: Int = 64,
        durationMs: Int = DEFAULT_ANIMATION_DURATION_MS
    ) {
        animationRequests.emit(CameraAnimationRequest.ToBounds(bounds, padding, durationMs))
    }

    /**
     * Instantly moves the camera to the given [position] without animation.
     */
    fun move(position: CameraPosition) {
        moveRequests.tryEmit(position)
    }
}

/**
 * Creates and remembers a [CameraPositionState], optionally applying an [init] block.
 *
 * @return A remembered [CameraPositionState].
 */
@Composable
fun rememberCameraPositionState(
    key: String? = null,
    init: CameraPositionState.() -> Unit = {}
): CameraPositionState = remember(key) { CameraPositionState().apply(init) }
