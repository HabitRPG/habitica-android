package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog

/**
 * Created by phillip on 27.09.17.
 */

abstract class InsufficientCurrencyDialog(context: Context) : HabiticaAlertDialog(context) {

    protected var imageView: ImageView
    protected var textView: TextView

    init {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_insufficient_currency, contentView, false)
        setAdditionalContentView(view)

        imageView = view.findViewById(R.id.imageView)
        textView = view.findViewById(R.id.textView)
    }

}
