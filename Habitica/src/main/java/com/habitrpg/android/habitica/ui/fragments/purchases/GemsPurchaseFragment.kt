package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.GiftGemsActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.promo.SubscriptionBuyGemsPromoView
import io.reactivex.functions.Consumer
import javax.inject.Inject

class GemsPurchaseFragment : BaseFragment(), GemPurchaseActivity.CheckoutFragment {

    private val gems4View: GemPurchaseOptionsView? by bindView(R.id.gems_4_view)
    private val gems21View: GemPurchaseOptionsView? by bindView(R.id.gems_21_view)
    private val gems42View: GemPurchaseOptionsView? by bindView(R.id.gems_42_view)
    private val gems84View: GemPurchaseOptionsView? by bindView(R.id.gems_84_view)
    private val subscriptionPromoView: SubscriptionBuyGemsPromoView? by bindView(R.id.subscription_promo)
    private val supportTextView: TextView? by bindView(R.id.supportTextView)
    private val giftGemsButton: Button? by bindView(R.id.gift_gems_button)

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository

    private var purchaseHandler: PurchaseHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        return container?.inflate(R.layout.fragment_gem_purchase)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gems4View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase4Gems) })
        gems21View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase21Gems) })
        gems42View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase42Gems) })
        gems84View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase84Gems) })

        val heartDrawable = BitmapDrawable(resources, HabiticaIconsHelper.imageOfHeartLarge())
        supportTextView?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, heartDrawable)

        compositeSubscription.add(userRepository.getUser().subscribe(Consumer {
            subscriptionPromoView?.visibility = if (it.isSubscribed) View.GONE else View.VISIBLE
        }, RxErrorHandler.handleEmptyError()))

        giftGemsButton?.setOnClickListener { showGiftGemsDialog() }
    }

    override fun setupCheckout() {
        purchaseHandler?.getAllGemSKUs { skus ->
            for (sku in skus) {
                updateButtonLabel(sku.id.code, sku.price)
            }
        }
    }

    override fun setPurchaseHandler(handler: PurchaseHandler?) {
        this.purchaseHandler = handler
    }

    private fun updateButtonLabel(sku: String, price: String) {
        val matchingView: GemPurchaseOptionsView? = when (sku) {
            PurchaseTypes.Purchase4Gems -> gems4View
            PurchaseTypes.Purchase21Gems -> gems21View
            PurchaseTypes.Purchase42Gems -> gems42View
            PurchaseTypes.Purchase84Gems -> gems84View
            else -> return
        }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(price)
            matchingView.sku = sku
        }
    }

    private fun purchaseGems(identifier: String) {
        purchaseHandler?.purchaseGems(identifier)
    }

    private fun showGiftGemsDialog() {
        val chooseRecipientDialogView = this.activity?.layoutInflater?.inflate(R.layout.dialog_choose_message_recipient, null)

        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.gift_title))
            alert.addButton(getString(R.string.action_continue), true) { _, _ ->
                val usernameEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                val intent = Intent(thisActivity, GiftGemsActivity::class.java).apply {
                    putExtra("username", usernameEditText?.text.toString())
                }
                startActivity(intent)
            }
            alert.addCancelButton { _, _ ->
                thisActivity.dismissKeyboard()
            }
            alert.setAdditionalContentView(chooseRecipientDialogView)
            alert.show()
        }
    }
}
