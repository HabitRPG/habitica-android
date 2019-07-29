package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class InsufficientHourglassesDialog(context: Context) : InsufficientCurrencyDialog(context) {
    init {
        imageView.setImageBitmap(HabiticaIconsHelper.imageOfHourglassShop())
        textView.setText(R.string.insufficientHourglasses)

        addButton(R.string.get_hourglasses, true) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity)  }
        addCloseButton()
    }
}
