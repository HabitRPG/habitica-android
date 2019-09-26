package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.MainNavigationController

/**
 * Created by phillip on 27.09.17.
 */

class InsufficientGemsDialog(context: Context) : InsufficientCurrencyDialog(context) {

    override var layoutID: Int = R.layout.dialog_insufficient_gems

    init {
        imageView.setImageResource(R.drawable.gems_84)
        textView.setText(R.string.insufficientGems)

        addButton(R.string.purchase_gems, true) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", true))) }
        addCloseButton()
    }
}
