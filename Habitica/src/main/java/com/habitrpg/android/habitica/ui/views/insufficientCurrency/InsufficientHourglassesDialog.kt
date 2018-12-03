package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import androidx.appcompat.app.AlertDialog

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class InsufficientHourglassesDialog(context: Context) : InsufficientCurrencyDialog(context) {
    init {

        imageView.setImageBitmap(HabiticaIconsHelper.imageOfHourglassShop())
        textView.setText(R.string.insufficientHourglasses)

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.get_hourglasses)) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity) }
    }
}
