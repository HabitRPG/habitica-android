package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by phillip on 27.09.17.
 */

class InsufficientGemsDialog(context: Context, var gemPrice: Int) : InsufficientCurrencyDialog(context) {

    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    override fun getLayoutID(): Int {
        return R.layout.dialog_insufficient_gems
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)
        textView.setText(R.string.insufficientGems)
        addButton(R.string.see_other_options, true) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false))) }
        addCloseButton()
        contentView.setPadding(0, 0, 0, 0)
        dialogContainer.setPadding(0, 0, 0, dialogContainer.paddingBottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getActivity()?.let { activity ->
            val purchaseTextView = contentView.findViewById<TextView>(R.id.purchase_textview)
            val purchaseButton = contentView.findViewById<Button>(R.id.purchase_button)
            purchaseHandler.startListening()
            val gemSku = if (gemPrice > 4) {
                purchaseTextView.text = "21"
                PurchaseTypes.Purchase21Gems
            } else {
                purchaseTextView.text = "4"
                PurchaseTypes.Purchase4Gems
            }
            CoroutineScope(Dispatchers.IO).launch {
                val sku = purchaseHandler.getInAppPurchaseSKU(gemSku)
                    ?: return@launch
                withContext(Dispatchers.Main) {
                    purchaseButton?.text = sku.oneTimePurchaseOfferDetails?.formattedPrice
                    contentView.findViewById<ProgressBar>(R.id.loading_indicator).isVisible = false
                    purchaseButton.isVisible = true

                    purchaseButton?.setOnClickListener {
                        FirebaseAnalytics.getInstance(context).logEvent(
                            "purchased_gems_from_insufficient",
                            bundleOf(Pair("gemPrice", gemPrice), Pair("sku", ""))
                        )
                        purchaseHandler.purchase(activity, sku)
                    }
                }
            }
        }
    }
}
