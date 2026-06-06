package uz.yalla.sdk.android.maps

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import uz.yalla.maps.api.MapProvider
import uz.yalla.maps.config.MapFactory
import uz.yalla.sdk.android.maps.google.GoogleMapProvider
import uz.yalla.sdk.android.maps.libre.LibreMapProvider

class YallaMapsFactory(application: Application) : MapFactory {

    @Volatile
    private var currentActivity: ComponentActivity? = null

    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                if (activity is ComponentActivity) currentActivity = activity
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity === activity) currentActivity = null
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

            override fun onActivityStarted(activity: Activity) = Unit

            override fun onActivityPaused(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) = Unit

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }

    override fun createGoogleProvider(): MapProvider = GoogleMapProvider()

    override fun createLibreProvider(): MapProvider = LibreMapProvider()
}
