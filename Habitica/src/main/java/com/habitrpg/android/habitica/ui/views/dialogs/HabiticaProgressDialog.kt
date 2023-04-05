package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.dpToPx

class HabiticaProgressDialog(context: Context) : HabiticaAlertDialog(context) {

    companion object {
        fun show(context: Context?, titleID: Int): HabiticaProgressDialog? {
            return show(context, context?.getString(titleID))
        }

        fun show(context: Context?, title: String?): HabiticaProgressDialog? {
            val dialog = context?.let { HabiticaProgressDialog(it) }
            dialog?.setAdditionalContentView(R.layout.circular_progress)
            dialog?.dialogWidth = 300.dpToPx(context)
            dialog?.setTitle(title)
            dialog?.enqueue()
            return dialog
        }
    }
}
