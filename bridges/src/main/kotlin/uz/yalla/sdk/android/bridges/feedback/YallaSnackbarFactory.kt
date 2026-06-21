package uz.yalla.sdk.android.bridges.feedback

import uz.yalla.components.config.feedback.SnackbarFactory

/**
 * Android implementation of the common `SnackbarFactory`.
 *
 * Toasts are rendered by [YallaSnackbarHost], which the host app must mount **once** at its content
 * root (see that composable's KDoc). [show]/[dismiss] are safe to call from any thread — they
 * marshal onto the main thread internally via [SnackbarController].
 */
class YallaSnackbarFactory : SnackbarFactory {
    override fun show(message: String, isError: Boolean) {
        SnackbarController.show(message, isError)
    }

    override fun dismiss() {
        SnackbarController.dismissAll()
    }
}
