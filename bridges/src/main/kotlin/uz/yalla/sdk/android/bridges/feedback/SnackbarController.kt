package uz.yalla.sdk.android.bridges.feedback

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

internal object SnackbarController {
    val items: SnapshotStateList<SnackbarItem> = mutableStateListOf()
    private var idCounter = 0L

    fun show(message: String, isError: Boolean) {
        items.clear()
        items.add(SnackbarItem(id = ++idCounter, message = message, isError = isError))
    }

    fun dismissAll() {
        items.clear()
    }
}
