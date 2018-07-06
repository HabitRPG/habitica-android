package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class InsufficientGoldDialog(context: Context) : InsufficientCurrencyDialog(context) {
    init {

        imageView.setImageBitmap(HabiticaIconsHelper.imageOfGoldReward())
        textView.text = context.getString(R.string.insufficientGold)
        setTitle(R.string.insufficientSubscriberGemsTitle)
    }
}
