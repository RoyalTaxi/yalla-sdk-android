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

    private fun nextKey(): String = "yalla_media_${keyCounter++}"

    private fun createCameraImageUri(context: Context): Uri? {
        val imagesDir = File(context.filesDir, CAMERA_IMAGE_DIR).apply { mkdirs() }
        val imageFile = File.createTempFile(CAMERA_IMAGE_PREFIX, CAMERA_IMAGE_SUFFIX, imagesDir)
        val authority = context.packageName + FILE_PROVIDER_SUFFIX
        return try {
            FileProvider.getUriForFile(context, authority, imageFile)
        } catch (e: IllegalArgumentException) {
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

internal fun resolvePickMode(selectionLimit: Int): PickMode =
    if (selectionLimit < 2) PickMode.Single else PickMode.Multiple(selectionLimit)
