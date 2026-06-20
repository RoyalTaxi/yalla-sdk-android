package uz.yalla.sdk.android.bridges.media

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import uz.yalla.media.config.MediaFactory
import java.io.File

/**
 * Android implementation of the common `MediaFactory`: image picking and camera capture routed
 * through the resumed [ComponentActivity]'s `ActivityResultRegistry`.
 *
 * **Camera capture integration contract.** [captureImage] hands the camera app a `content://` URI
 * produced by a [FileProvider] the **host app** must declare, at authority
 * `"${applicationId}.provider"`, exposing the app's internal files directory. The bridges library
 * intentionally ships no provider of its own (the authority is per-app and would collide with the
 * host's). The host manifest must contain:
 * ```xml
 * <provider
 *     android:name="androidx.core.content.FileProvider"
 *     android:authorities="${applicationId}.provider"
 *     android:exported="false"
 *     android:grantUriPermissions="true">
 *     <meta-data
 *         android:name="android.support.FILE_PROVIDER_PATHS"
 *         android:resource="@xml/file_paths" />
 * </provider>
 * ```
 * with a `res/xml/file_paths.xml` containing `<files-path name="share_images" path="share_images/" />`.
 * If the provider is missing, [captureImage] fails with a clear [IllegalStateException].
 */
class YallaMediaFactory(application: Application) : MediaFactory {
    private var currentActivity: ComponentActivity? = null
    private var keyCounter = 0L

    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                if (activity is ComponentActivity) currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity === activity) currentActivity = null
            }
        })
    }

    override fun pickImages(selectionLimit: Int, onResult: (List<Uri>) -> Unit) {
        val activity = currentActivity ?: run {
            onResult(emptyList())
            return
        }
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

        when (val mode = resolvePickMode(selectionLimit)) {
            PickMode.Single -> {
                lateinit var launcher: ActivityResultLauncher<PickVisualMediaRequest>
                launcher = activity.activityResultRegistry.register(
                    nextKey(),
                    ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    launcher.unregister()
                    onResult(listOfNotNull(uri))
                }
                launcher.launch(request)
            }

            is PickMode.Multiple -> {
                lateinit var launcher: ActivityResultLauncher<PickVisualMediaRequest>
                launcher = activity.activityResultRegistry.register(
                    nextKey(),
                    ActivityResultContracts.PickMultipleVisualMedia(mode.maxItems)
                ) { uris ->
                    launcher.unregister()
                    onResult(uris)
                }
                launcher.launch(request)
            }
        }
    }

    override fun captureImage(onResult: (Uri?) -> Unit) {
        val activity = currentActivity ?: run {
            onResult(null)
            return
        }
        val uri = createCameraImageUri(activity) ?: run {
            onResult(null)
            return
        }
        lateinit var launcher: ActivityResultLauncher<Uri>
        launcher = activity.activityResultRegistry.register(
            nextKey(),
            ActivityResultContracts.TakePicture()
        ) { success ->
            launcher.unregister()
            onResult(if (success) uri else null)
        }
        launcher.launch(uri)
    }

    // Monotonically increasing so each ActivityResult launcher gets a unique registry key; the
    // uniqueness is load-bearing because ActivityResultRegistry rejects a re-used key. Long never
    // realistically overflows here.
    private fun nextKey(): String = "yalla_media_${keyCounter++}"

    private fun createCameraImageUri(context: Context): Uri? {
        val imagesDir = File(context.filesDir, CAMERA_IMAGE_DIR).apply { mkdirs() }
        // Each capture gets a unique temp file (createTempFile guarantees uniqueness). We intentionally
        // do NOT wipe the directory first: a previous capture's URI may still be read by a consumer
        // (e.g. an upload in flight), and deleting it would invalidate that read. The files live in
        // app-internal storage and are reclaimed on clear-data / uninstall.
        val imageFile = File.createTempFile(CAMERA_IMAGE_PREFIX, CAMERA_IMAGE_SUFFIX, imagesDir)
        val authority = context.packageName + FILE_PROVIDER_SUFFIX
        return try {
            FileProvider.getUriForFile(context, authority, imageFile)
        } catch (e: IllegalArgumentException) {
            // The host app must declare a FileProvider for camera capture to work — see the class
            // KDoc. Surface a clear, actionable message instead of the opaque framework one.
            throw IllegalStateException(
                "captureImage() requires the host app to declare a FileProvider at authority " +
                    "\"$authority\" exposing the app's internal files dir (a \"$CAMERA_IMAGE_DIR\" " +
                    "<files-path>). See YallaMediaFactory KDoc for the exact <provider> declaration.",
                e
            )
        }
    }

    private companion object {
        const val CAMERA_IMAGE_DIR = "share_images"
        const val CAMERA_IMAGE_PREFIX = "camera_"
        const val CAMERA_IMAGE_SUFFIX = ".jpg"
        const val FILE_PROVIDER_SUFFIX = ".provider"
    }
}

internal sealed interface PickMode {
    data object Single : PickMode
    data class Multiple(val maxItems: Int) : PickMode
}

/**
 * Maps the public `selectionLimit` contract onto a pick mode. `PickMultipleVisualMedia` requires
 * `maxItems >= 2`, and a non-positive limit is meaningless, so any limit below 2 resolves to a
 * single pick (previously a limit of 0 or negative silently became an *unbounded* multi-pick).
 */
internal fun resolvePickMode(selectionLimit: Int): PickMode =
    if (selectionLimit < 2) PickMode.Single else PickMode.Multiple(selectionLimit)
