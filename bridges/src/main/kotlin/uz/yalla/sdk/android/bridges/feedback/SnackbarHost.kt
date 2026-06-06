package uz.yalla.sdk.android.bridges.feedback

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

internal class SnackbarHost(application: Application) {
    private val items = mutableStateListOf<SnackbarItem>()
    private var currentActivity: Activity? = null
    private var dialog: Dialog? = null
    private var idCounter = 0L

    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                if (items.isNotEmpty()) ensureDialog()
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity === activity) {
                    tearDownDialog()
                }
            }

            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity === activity) {
                    currentActivity = null
                }
            }
        })
    }

    fun show(message: String, isError: Boolean) {
        if (items.size >= 3) items.removeAt(0)
        items.add(SnackbarItem(id = ++idCounter, message = message, isError = isError))
        ensureDialog()
    }

    fun dismissAll() {
        items.clear()
        tearDownDialog()
    }

    private fun ensureDialog() {
        if (dialog?.isShowing == true) return
        val activity = currentActivity ?: return
        val decor = activity.window?.decorView
        val lifecycleOwner = decor?.findViewTreeLifecycleOwner()
        val savedStateOwner = decor?.findViewTreeSavedStateRegistryOwner()

        val composeView = ComposeView(activity).apply {
            if (lifecycleOwner != null) setViewTreeLifecycleOwner(lifecycleOwner)
            if (savedStateOwner != null) setViewTreeSavedStateRegistryOwner(savedStateOwner)
            setContent { SnackbarOverlay(items = items) }
        }

        val newDialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar).apply {
            setContentView(
                composeView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setDimAmount(0f)
                setGravity(Gravity.TOP)
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
            setCanceledOnTouchOutside(false)
        }
        newDialog.show()
        dialog = newDialog
    }

    private fun tearDownDialog() {
        dialog?.dismiss()
        dialog = null
    }
}
