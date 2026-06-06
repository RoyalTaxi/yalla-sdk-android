package uz.yalla.sdk.android.bridges.feedback

import android.app.Application
import uz.yalla.components.config.feedback.SnackbarFactory

class YallaSnackbarFactory(application: Application) : SnackbarFactory {
    private val host = SnackbarHost(application)

    override fun show(message: String, isError: Boolean) {
        host.show(message, isError)
    }

    override fun dismiss() {
        host.dismissAll()
    }
}
