package uz.yalla.sdk.android.bridges.telemetry

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import uz.yalla.telemetry.event.AnalyticsEvent
import uz.yalla.telemetry.event.ParamValue
import uz.yalla.telemetry.sink.TelemetrySink

/** Android `TelemetrySink` that forwards analytics events and the active user id to Firebase Analytics. */
class FirebaseAnalyticsSink(context: Context) : TelemetrySink {
    private val analytics = FirebaseAnalytics.getInstance(context.applicationContext)

    override fun track(event: AnalyticsEvent) {
        analytics.logEvent(event.name, event.params.toBundle())
    }

    override fun setUser(userId: String?) {
        analytics.setUserId(userId)
    }

    private fun Map<String, ParamValue>.toBundle(): Bundle? {
        if (isEmpty()) return null
        val bundle = Bundle(size)
        for ((key, value) in this) {
            when (value) {
                is ParamValue.Text -> bundle.putString(key, value.value)
                is ParamValue.Count -> bundle.putLong(key, value.value)
                is ParamValue.Amount -> bundle.putDouble(key, value.value)
                is ParamValue.Flag -> bundle.putBoolean(key, value.value)
            }
        }
        return bundle
    }
}
