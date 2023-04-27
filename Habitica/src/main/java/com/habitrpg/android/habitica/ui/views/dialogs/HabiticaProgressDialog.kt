package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.dpToPx

class HabiticaProgressDialog(context: Context) : HabiticaAlertDialog(context) {

    companion object {
        fun show(context: Context, titleID: Int): HabiticaProgressDialog {
            return show(context, context.getString(titleID))
        }

        fun show(context: Context, title: String?, dialogWidth: Int = 300): HabiticaProgressDialog {
            val dialog = HabiticaProgressDialog(context)
            dialog.setAdditionalContentView(R.layout.circular_progress)
            dialog.dialogWidth = dialogWidth.dpToPx(context)
            dialog.setTitle(title)
            dialog.enqueue()
            return dialog
        }
    }
}
