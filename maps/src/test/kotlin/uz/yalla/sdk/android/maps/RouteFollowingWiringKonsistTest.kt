package uz.yalla.sdk.android.maps

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Pins the route-following feature-flag wiring on the production map controllers.
 *
 * `MarkerMotionDriver` (and the `DriverMotionModel` it builds) defaults `routeFollowingEnabled` to
 * **false** on purpose — it is the kill switch that keeps chord interpolation the safe default (SDK
 * ADR 0003). The production controllers must therefore *opt in* by constructing the driver with
 * `routeFollowingEnabled = true`; if they do not, the car silently cuts corners instead of eating /
 * snapping to the route.
 *
 * That opt-in is load-bearing and has regressed twice: it was never set on Android (fixed in
 * `ebfbbba`) and was lost to Swift lazy-capture on iOS (fixed in `258b21c`). The model default is
 * pinned by its own tests, but the *flag-ON wiring* was pinned by nothing — a future "constructor
 * cleanup" could drop it and CI would stay green. iOS now makes the flag an init-only `let` so the
 * type system guards it; this test is the Android equivalent. Any controller that builds a
 * `MarkerMotionDriver` must enable route-following.
 */
class RouteFollowingWiringKonsistTest {
    @Test
    fun controllersThatBuildTheMotionDriverEnableRouteFollowing() {
        Konsist
            .scopeFromDirectory(CONTROLLER_SOURCE_DIR)
            .files
            .filter { file -> file.nameWithExtension.endsWith("Controller.kt") }
            .filter { file -> file.text.contains("$MOTION_DRIVER(") }
            .assertTrue { file -> file.text.contains(ROUTE_FOLLOWING_ON) }
    }

    private companion object {
        /**
         * Directory scope (relative to the Gradle root): in this composite build `scopeFromProject()`
         * resolves to the included SDK build's common sources, not the platform controllers that live
         * here — so a directory scope is required to actually see `AndroidGoogleMapController` /
         * `AndroidLibreMapController`. Mirrors `RendererPurityKonsistTest`.
         */
        const val CONTROLLER_SOURCE_DIR = "maps/src/main"
        const val MOTION_DRIVER = "MarkerMotionDriver"
        const val ROUTE_FOLLOWING_ON = "routeFollowingEnabled = true"
    }
}
