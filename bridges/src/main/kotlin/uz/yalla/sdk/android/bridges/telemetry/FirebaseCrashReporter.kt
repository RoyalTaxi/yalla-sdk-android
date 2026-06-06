package uz.yalla.sdk.android.bridges.telemetry

import com.google.firebase.crashlytics.FirebaseCrashlytics
import uz.yalla.telemetry.crash.CrashReporter

class FirebaseCrashReporter : CrashReporter {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun record(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun setUser(userId: String?) {
        crashlytics.setUserId(userId.orEmpty())
    }
}
