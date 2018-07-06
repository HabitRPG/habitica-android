package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView

/**
 * Created by phillip on 27.09.17.
 */

abstract class InsufficientCurrencyDialog(context: Context) : AlertDialog(context) {

    protected val imageView: ImageView by bindView(R.id.imageView)
    protected val textView: TextView by bindView(R.id.textView)

    init {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_insufficient_currency, null)
        setView(view)

        this.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.close)) { _, _ -> this.dismiss() }
    }

}
