package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.HabiticaPurchaseVerifier
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.*
import javax.inject.Inject


class GiftIAPActivity: BaseActivity() {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    var activityCheckout: ActivityCheckout? = null
        private set
    private var billingRequests: BillingRequests? = null

    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private val giftOneGetOneContainer: ViewGroup? by bindView(R.id.gift_subscription_container)

    private val avatarView: AvatarView by bindView(R.id.avatar_view)
    private val displayNameTextView: UsernameLabel by bindView(R.id.display_name_textview)
    private val usernameTextView: TextView by bindView(R.id.username_textview)

    private val subscription1MonthView: SubscriptionOptionView? by bindView(R.id.subscription1month)
    private val subscription3MonthView: SubscriptionOptionView? by bindView(R.id.subscription3month)
    private val subscription6MonthView: SubscriptionOptionView? by bindView(R.id.subscription6month)
    private val subscription12MonthView: SubscriptionOptionView? by bindView(R.id.subscription12month)

    private val subscriptionButton: Button? by bindOptionalView(R.id.subscribeButton)

    private var giftedUsername: String? = null
    private var giftedUserID: String? = null

    private var selectedSubscriptionSku: Sku? = null
    private var skus: List<Sku> = emptyList()

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift_iap
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.gift_subscription)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        giftedUsername = intent.getStringExtra("username")

        subscriptionButton?.setOnClickListener {
            selectedSubscriptionSku?.let { sku -> purchaseSubscription(sku) }
        }

        giftOneGetOneContainer?.isVisible = appConfigManager.enableGiftOneGetOne()

        compositeSubscription.add(socialRepository.getMemberWithUsername(giftedUsername).subscribe(Consumer {
            avatarView.setAvatar(it)
            displayNameTextView.username = it.profile?.name
            displayNameTextView.tier = it.contributor?.level ?: 0
            usernameTextView.text = "@${it.username}"
            giftedUserID = it.id
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun onStart() {
        super.onStart()
        setupCheckout()

        activityCheckout?.destroyPurchaseFlow()

        activityCheckout?.createPurchaseFlow(object : RequestListener<Purchase> {
            override fun onSuccess(purchase: Purchase) {
                if (PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
                    billingRequests?.consume(purchase.token, object : RequestListener<Any> {
                        override fun onSuccess(o: Any) {
                            finish()
                        }

                        override fun onError(i: Int, e: Exception) {
                            crashlyticsProxy.fabricLogE("PurchaseConsumeException", "Consume", e)
                        }
                    })
                }
            }

            override fun onError(i: Int, e: Exception) {
                crashlyticsProxy.fabricLogE("PurchaseFlowException", "Error", e)
            }
        })


        activityCheckout?.whenReady(object : Checkout.Listener {
            override fun onReady(billingRequests: BillingRequests) {
                this@GiftIAPActivity.billingRequests = billingRequests
            }

            override fun onReady(billingRequests: BillingRequests, s: String, b: Boolean) {}
        })
    }

    override fun onResume() {
        super.onResume()

        this.subscription1MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription1MonthNoRenew) })
        this.subscription3MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription3MonthNoRenew) })
        this.subscription6MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription6MonthNoRenew) })
        this.subscription12MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription12MonthNoRenew) })
    }

    override fun onStop() {
        activityCheckout?.stop()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityCheckout?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateButtonLabel(sku: Sku, price: String, subscriptions: Inventory.Product) {
        val matchingView = buttonForSku(sku)
        if (matchingView != null) {
            matchingView.setPriceText(price)
            matchingView.sku = sku.id.code
            matchingView.setIsPurchased(subscriptions.isPurchased(sku))
        }
    }

    private fun setupCheckout() {
        HabiticaBaseApplication.getInstance(this)?.billing?.let {
            activityCheckout = Checkout.forActivity(this, it)
            activityCheckout?.start()
        }
        val checkout = activityCheckout
        if (checkout != null) {
            val inventory = checkout.makeInventory()

            inventory.load(Inventory.Request.create()
                    .loadAllPurchases().loadSkus(ProductTypes.IN_APP, PurchaseTypes.allSubscriptionNoRenewTypes)
            ) { products ->
                val subscriptions = products.get(ProductTypes.IN_APP)

                skus = subscriptions.skus

                for (sku in skus) {
                    updateButtonLabel(sku, sku.price, subscriptions)
                }
                selectSubscription(PurchaseTypes.Subscription1MonthNoRenew)
            }
        }
    }

    private fun selectSubscription(sku: String) {
        for (thisSku in skus) {
            if (thisSku.id.code == sku) {
                selectSubscription(thisSku)
                return
            }
        }
    }

    private fun selectSubscription(sku: Sku) {
        for (thisSku in skus) {
            buttonForSku(thisSku)?.setIsPurchased(false)
        }
        this.selectedSubscriptionSku = sku
        val subscriptionOptionButton = buttonForSku(this.selectedSubscriptionSku)
        subscriptionOptionButton?.setIsPurchased(true)
        if (this.subscriptionButton != null) {
            this.subscriptionButton?.isEnabled = true
        }
    }

    private fun buttonForSku(sku: Sku?): SubscriptionOptionView? {
        return buttonForSku(sku?.id?.code)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.Subscription1MonthNoRenew -> subscription1MonthView
            PurchaseTypes.Subscription3MonthNoRenew -> subscription3MonthView
            PurchaseTypes.Subscription6MonthNoRenew -> subscription6MonthView
            PurchaseTypes.Subscription12MonthNoRenew -> subscription12MonthView
            else -> null
        }
    }

    private fun purchaseSubscription(sku: Sku) {
        if (giftedUserID?.isNotEmpty() != true) {
            return
        }
        activityCheckout?.let {
            HabiticaPurchaseVerifier.pendingGifts[sku.id.code] = giftedUserID
            billingRequests?.purchase(ProductTypes.IN_APP, sku.id.code, null, it.purchaseFlow)
        }
    }


    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        consumePurchase(event.purchase)
        runOnUiThread {
            displayConfirmationDialog()
        }
    }

    private fun selectedDurationString(): String {
        return when (selectedSubscriptionSku?.id?.code) {
            PurchaseTypes.Subscription1MonthNoRenew -> "1"
            PurchaseTypes.Subscription3MonthNoRenew -> "3"
            PurchaseTypes.Subscription6MonthNoRenew -> "6"
            PurchaseTypes.Subscription12MonthNoRenew -> "12"
            else -> ""
        }
    }

    private fun displayConfirmationDialog() {
        val message = getString(if (appConfigManager.enableGiftOneGetOne()){
            R.string.gift_confirmation_text_g1g1
        } else {
            R.string.gift_confirmation_text
        }, giftedUsername, selectedDurationString())
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.gift_confirmation_title)
        alert.setMessage(message)
        alert.addOkButton { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
        alert.show()
    }

    private fun consumePurchase(purchase: Purchase) {
        if (PurchaseTypes.allGemTypes.contains(purchase.sku) || PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
            billingRequests?.consume(purchase.token, object : RequestListener<Any> {

                override fun onSuccess(result: Any) {
                }

                override fun onError(response: Int, e: Exception) {
                    crashlyticsProxy.fabricLogE("PurchaseConsumeException", "Consume", e)
                }
            })
        }
    }
}