package uz.yalla.sdk.android.maps

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/**
 * Renderer-purity fitness function for the Android map renderers.
 *
 * Route-eating, projection, snapping and off-route detection live in the SDK model
 * ([uz.yalla.maps.motion.DriverMotionModel] / [uz.yalla.core.geo.RouteProgressGeometry]). The
 * Android `*Controller` renderers are Humble Objects: they draw only the pose, the already-trimmed
 * `remainingRoute`, and the `connector` line the model hands back. This test pins that boundary so the
 * "route behind the car" policy cannot silently re-scatter back into a renderer — a `*Renderer` or
 * `*Controller` Kotlin file must not import the route geometry math.
 *
 * Mirrors the SDK-side `uz.yalla.konsist.RendererPurityKonsistTest`, which guards the common/Android
 * Kotlin inside the `yalla-sdk` build; this one guards the renderer sources that live in this repo
 * (the platform map controllers), which the SDK scope does not see. The same invariant is enforced for
 * the iOS Swift renderers by manual review (ADR 0003) since Konsist sees only Kotlin.
 */
class RendererPurityKonsistTest {
    @Test
    fun renderersAndControllersDoNotImportRouteGeometryMath() {
        Konsist
            .scopeFromDirectory(RENDERER_SOURCE_DIR)
            .files
            .filter { file ->
                val name = file.nameWithExtension
                name.endsWith("Renderer.kt") || name.endsWith("Controller.kt")
            }.assertFalse { file ->
                file.hasImport { import ->
                    FORBIDDEN_GEO_MATH_IMPORTS.any { forbidden -> import.name == forbidden }
                }
            }
    }

    private companion object {
        /**
         * Scoped to this module's renderer sources by directory (relative to the Gradle root). In this
         * composite build `scopeFromProject()` resolves to the included SDK build's common sources, not
         * the platform controllers that live here — so a directory scope is required to actually see
         * (and pin) `AndroidGoogleMapController` / `AndroidLibreMapController`.
         */
        const val RENDERER_SOURCE_DIR = "maps/src/main"

        val FORBIDDEN_GEO_MATH_IMPORTS =
            listOf(
                "uz.yalla.core.geo.RouteProgressGeometry",
                "uz.yalla.core.geo.RouteGeometry",
                "uz.yalla.core.geo.routeProgress",
                "uz.yalla.core.geo.headingAlongRoute",
                "uz.yalla.core.geo.distanceTo",
                "uz.yalla.core.geo.bearingTo"
            )
    }
}
