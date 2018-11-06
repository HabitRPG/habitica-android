package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import androidx.appcompat.app.AlertDialog

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

import org.greenrobot.eventbus.EventBus

class InsufficientHourglassesDialog(context: Context) : InsufficientCurrencyDialog(context) {
    init {

        imageView.setImageBitmap(HabiticaIconsHelper.imageOfHourglassShop())
        textView.setText(R.string.insufficientHourglasses)

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.get_hourglasses)) { _, _ -> EventBus.getDefault().post(OpenGemPurchaseFragmentCommand()) }
    }
}
