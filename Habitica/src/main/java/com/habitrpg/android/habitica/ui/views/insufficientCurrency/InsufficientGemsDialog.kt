package com.habitrpg.android.habitica.ui.views.insufficientCurrency

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

/**
 * Created by phillip on 27.09.17.
 */

class InsufficientGemsDialog(context: Context) : InsufficientCurrencyDialog(context) {

    private var purchaseButton: Button? = null
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy

    private var purchaseHandler: PurchaseHandler? = null

    override fun getLayoutID(): Int {
        return R.layout.dialog_insufficient_gems
    }

    init {
        HabiticaBaseApplication.userComponent?.inject(this)
        imageView.setImageResource(R.drawable.gems_84)
        textView.setText(R.string.insufficientGems)


        addButton(R.string.see_other_options, false) { _, _ -> MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false))) }
        addCloseButton()
        if (!configManager.insufficientGemPurchase()) {
            contentView.findViewWithTag<LinearLayout>(R.id.purchase_wrapper).visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (configManager.insufficientGemPurchase()) {
            getActivity()?.let {
                purchaseButton = contentView.findViewById(R.id.purchase_button)
                purchaseHandler = PurchaseHandler(it, crashlyticsProxy)
                purchaseHandler?.startListening()
                purchaseHandler?.whenCheckoutReady = {
                    purchaseHandler?.getInAppPurchaseSKU(PurchaseTypes.Purchase4Gems) {sku ->
                        val purchaseTextView = contentView.findViewById<TextView>(R.id.purchase_textview)
                        purchaseTextView.text = sku.displayTitle
                        purchaseButton?.text = sku.price
                    }
                }

                purchaseButton?.setOnClickListener {
                    purchaseHandler?.purchaseGems(PurchaseTypes.Purchase4Gems)
                }
            }
        }
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDetachedFromWindow()
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        purchaseHandler?.consumePurchase(event.purchase)
        dismiss()
    }
}
