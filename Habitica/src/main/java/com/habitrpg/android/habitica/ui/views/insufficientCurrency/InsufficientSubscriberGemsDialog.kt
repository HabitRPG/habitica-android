package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class InsufficientSubscriberGemsDialog(context: Context) : InsufficientCurrencyDialog(context) {
    init {

        imageView.setImageBitmap(HabiticaIconsHelper.imageOfGem_36())
        textView.text = context.getString(R.string.insufficientSubscriberGems)
    }
}
