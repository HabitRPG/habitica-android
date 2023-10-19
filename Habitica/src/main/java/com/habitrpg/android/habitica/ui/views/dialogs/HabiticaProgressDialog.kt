package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView
import com.habitrpg.common.habitica.extensions.dpToPx

class HabiticaProgressDialog(context: Context) : HabiticaAlertDialog(context) {

    companion object {
        fun show(context: Context, titleID: Int): HabiticaProgressDialog {
            return show(context, context.getString(titleID))
        }

        fun show(context: Context, title: String?, dialogWidth: Int = 300): HabiticaProgressDialog {
            val dialog = HabiticaProgressDialog(context)
            val composeView = ComposeView(context)
            dialog.setAdditionalContentView(composeView)
            composeView.setContent {
                HabiticaTheme {
                    HabiticaCircularProgressView(Modifier.size(60.dp))
                }
            }
            dialog.dialogWidth = dialogWidth.dpToPx(context)
            dialog.setTitle(title)
            dialog.enqueue()
            return dialog
        }
    }
}
