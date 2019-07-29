package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import com.habitrpg.android.habitica.R

class InsufficientSubscriberGemsDialog(context: Context) : InsufficientCurrencyDialog(context) {
    init {
        imageView.setImageResource(R.drawable.subscriber_gem_cap)
        textView.text = context.getString(R.string.insufficientSubscriberGems)
        addButton(R.string.take_me_back, true)
    }
}
