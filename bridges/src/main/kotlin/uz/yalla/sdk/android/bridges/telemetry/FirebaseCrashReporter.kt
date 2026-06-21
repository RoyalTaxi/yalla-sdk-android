package uz.yalla.sdk.android.bridges.telemetry

import com.google.firebase.crashlytics.FirebaseCrashlytics
import uz.yalla.telemetry.crash.CrashReporter

/** Android `CrashReporter` that records non-fatal throwables and the active user id to Crashlytics. */
class FirebaseCrashReporter : CrashReporter {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun record(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    /**
     * Associates subsequent crash reports with [userId], or clears the association when it is
     * `null`. Crashlytics has no nullable setter, so `null` is mapped to `""`, which is its
     * documented "clear the user" value — matching `FirebaseAnalyticsSink.setUser(null)`.
     */
    override fun setUser(userId: String?) {
        crashlytics.setUserId(userId.orEmpty())
    }
}
