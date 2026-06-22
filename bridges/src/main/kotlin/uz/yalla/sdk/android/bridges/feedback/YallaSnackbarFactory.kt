package uz.yalla.sdk.android.bridges.feedback

import uz.yalla.components.config.feedback.SnackbarFactory

class YallaSnackbarFactory : SnackbarFactory {
    override fun show(message: String, isError: Boolean) {
        SnackbarController.show(message, isError)
    }

    override fun dismiss() {
        SnackbarController.dismissAll()
    }
}
