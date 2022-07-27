package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by phillip on 27.09.17.
 */

class InsufficientGemsDialog(context: Context, var gemPrice: Int) : InsufficientCurrencyDialog(context) {

    private var purchaseButton: Button? = null
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    override fun getLayoutID(): Int {
        return R.layout.dialog_insufficient_gems
    }

    var sku: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)
        imageView.setImageResource(R.drawable.gems_84)
        textView.setText(R.string.insufficientGems)
        addCloseButton()
    }



    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getActivity()?.let { activity ->
            if (configManager.insufficientGemPurchase()) {
                purchaseButton = contentView.findViewById(R.id.purchase_button)
                purchaseHandler.startListening()
                sku = if (configManager.insufficientGemPurchaseAdjust()) {
                    if (gemPrice > 4) {
                        PurchaseTypes.Purchase21Gems
                    } else {
                        PurchaseTypes.Purchase4Gems
                    }
                } else {
                    PurchaseTypes.Purchase4Gems
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val sku = purchaseHandler.getInAppPurchaseSKU(PurchaseTypes.Purchase4Gems)
                        ?: return@launch
                    withContext(Dispatchers.Main) {
                        val purchaseTextView =
                            contentView.findViewById<TextView>(R.id.purchase_textview)
                        purchaseTextView.text = sku.title
                        purchaseButton?.text = sku.price

                        purchaseButton?.setOnClickListener {
                            FirebaseAnalytics.getInstance(context).logEvent(
                                "purchased_gems_from_insufficient",
                                bundleOf(Pair("gemPrice", gemPrice), Pair("sku", ""))
                            )
                            purchaseHandler.purchase(activity, sku)
                        }
                    }
                }

                addButton(R.string.see_other_options, false) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false))) }
            } else {
                contentView.findViewById<LinearLayout>(R.id.purchase_wrapper).visibility = View.GONE
                addButton(R.string.purchase_gems, false) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false))) }
            }
        }
    }
}
