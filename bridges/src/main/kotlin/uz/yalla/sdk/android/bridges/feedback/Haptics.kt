package uz.yalla.sdk.android.bridges.feedback

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Centralized haptic feedback for the Android component layer, the peer of the iOS `Haptics` enum.
 *
 * Android users expect a tactile confirmation on meaningful actions — a success/error toast, a
 * confirmation tap, a selection, a destructive choice. The native layer previously emitted none,
 * which is one of the clearest "this isn't a real app" signals.
 *
 * Each call routes through [View.performHapticFeedback], which the platform silently no-ops when
 * the user has disabled haptics, so no extra guard is needed. Call from a composable with the
 * `LocalView.current` instance; the constants used here are available from API 26 (the module's
 * `minSdk`) onward, with newer semantic constants (`CONFIRM`/`REJECT`) gated behind their API level
 * and falling back gracefully.
 */
internal object Haptics {
    /** A completed, positive action (e.g. a code accepted, a toast confirming success). */
    fun success(view: View) {
        view.performHapticFeedback(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
        )
    }

    /** A failed action (e.g. a rejected code, an error toast). */
    fun error(view: View) {
        view.performHapticFeedback(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                HapticFeedbackConstants.REJECT
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
        )
    }

    /** A cautionary or destructive action (e.g. tapping a delete row). */
    fun warning(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /** Moving between discrete options (e.g. picking a row in a selection sheet). */
    fun selection(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /** A physical "tap" for button presses and commits (primary confirmations, date-done). */
    fun impact(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}
