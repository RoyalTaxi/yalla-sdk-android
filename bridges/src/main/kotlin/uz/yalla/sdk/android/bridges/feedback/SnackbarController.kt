package uz.yalla.sdk.android.bridges.feedback

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

internal object SnackbarController {
    val items: SnapshotStateList<SnackbarItem> = mutableStateListOf()

    private var idCounter = 0L

    private val mainHandler = Handler(Looper.getMainLooper())

    fun show(message: String, isError: Boolean) = onMain {
        items.clear()
        items.add(SnackbarItem(id = ++idCounter, message = message, isError = isError))
    }

    fun dismissAll() = onMain {
        items.clear()
    }

    private inline fun onMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post { block() }
        }
    }
}
