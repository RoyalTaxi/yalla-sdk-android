package uz.yalla.sdk.android.bridges.feedback

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Backing store for the snackbar overlay.
 *
 * [items] is a Compose [SnapshotStateList], so every mutation is a snapshot write and **must** happen
 * on the main thread. [SnackbarFactory.show]/`dismiss` carry no thread contract and are routinely
 * called off-main (e.g. a network error observed on `Dispatchers.IO`), so [show]/[dismissAll] marshal
 * onto the main thread — running inline when already on it — mirroring the iOS factory's `onMain`.
 */
internal object SnackbarController {
    val items: SnapshotStateList<SnackbarItem> = mutableStateListOf()

    /** Monotonic id used as the Compose `key` for each toast so reuse animates correctly. */
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
