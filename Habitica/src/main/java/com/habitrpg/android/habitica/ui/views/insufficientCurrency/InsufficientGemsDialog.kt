package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.app.AlertDialog
import android.content.Context

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand

import org.greenrobot.eventbus.EventBus

/**
 * Created by phillip on 27.09.17.
 */

class InsufficientGemsDialog(context: Context) : InsufficientCurrencyDialog(context) {

    init {

        imageView.setImageResource(R.drawable.gems_84)
        textView.setText(R.string.insufficientGems)

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.purchase_gems)) { _, _ -> EventBus.getDefault().post(OpenGemPurchaseFragmentCommand()) }
    }
}
