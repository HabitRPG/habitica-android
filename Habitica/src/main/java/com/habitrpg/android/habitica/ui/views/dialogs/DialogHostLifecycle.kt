package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.cancel

internal fun ComponentDialog.cancelWorkWhenHostDestroyed(onHostDestroyed: () -> Unit = {}) {
    val hostLifecycle = findHostActivity()?.lifecycle ?: return
    val dialogLifecycle = lifecycle
    val hostObserver =
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                onHostDestroyed()
                lifecycleScope.cancel()
            }
        }
    hostLifecycle.addObserver(hostObserver)
    dialogLifecycle.addObserver(
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                hostLifecycle.removeObserver(hostObserver)
            }
        },
    )
}

private fun ComponentDialog.findHostActivity(): ComponentActivity? {
    var current = context
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}
