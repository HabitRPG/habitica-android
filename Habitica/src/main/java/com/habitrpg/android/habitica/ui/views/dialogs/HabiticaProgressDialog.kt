package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.CircularProgressComposable

class HabiticaProgressDialog(context: Context) : HabiticaAlertDialog(context) {
    companion object {
        fun show(
            context: FragmentActivity,
            titleID: Int,
        ): HabiticaProgressDialog {
            return show(context, context.getString(titleID))
        }

        fun show(
            context: FragmentActivity,
            title: String?,
            dialogWidth: Int = 300,
        ): HabiticaProgressDialog {
            val dialog = HabiticaProgressDialog(context)
            val composeView = ComposeView(context)
            dialog.setAdditionalContentView(composeView)
            composeView.setContent {
                HabiticaTheme {
                    CircularProgressComposable(Modifier.size(60.dp))
                }
            }
            dialog.window?.let {
                dialog.additionalContentView?.setViewTreeSavedStateRegistryOwner(context)
                it.decorView.setViewTreeSavedStateRegistryOwner(context)
                dialog.additionalContentView?.setViewTreeLifecycleOwner(context)
                it.decorView.setViewTreeLifecycleOwner(context)
            }
            dialog.dialogWidth = dialogWidth.dpToPx(context)
            dialog.setTitle(title)
            dialog.enqueue()
            return dialog
        }
    }
}
