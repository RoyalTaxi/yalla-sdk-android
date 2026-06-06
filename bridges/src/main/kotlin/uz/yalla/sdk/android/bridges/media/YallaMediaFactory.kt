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

        if (selectionLimit == 1) {
            lateinit var launcher: ActivityResultLauncher<PickVisualMediaRequest>
            launcher = activity.activityResultRegistry.register(
                nextKey(),
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                launcher.unregister()
                onResult(listOfNotNull(uri))
            }
            launcher.launch(request)
        } else {
            val contract = if (selectionLimit <= 1) {
                ActivityResultContracts.PickMultipleVisualMedia()
            } else {
                ActivityResultContracts.PickMultipleVisualMedia(selectionLimit)
            }
            lateinit var launcher: ActivityResultLauncher<PickVisualMediaRequest>
            launcher = activity.activityResultRegistry.register(nextKey(), contract) { uris ->
                launcher.unregister()
                onResult(uris)
            }
            launcher.launch(request)
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
        val imagesDir = File(context.filesDir, "share_images").apply { mkdirs() }
        imagesDir.listFiles()?.forEach { it.delete() }
        val imageFile = File.createTempFile("camera_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(context, context.packageName + ".provider", imageFile)
    }
}
