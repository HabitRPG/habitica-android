package com.habitrpg.android.habitica.ui.fragments

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.UserSubscribedEvent
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.GiftIAPActivity
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_subscription.*
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.*
import javax.inject.Inject

class SubscriptionFragment : BaseFragment(), GemPurchaseActivity.CheckoutFragment {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private val giftOneGetOneContainer: ViewGroup? by bindView(R.id.gift_subscription_container)
    private val giftOneGetOneButton: Button? by bindView(R.id.gift_subscription_promo_button)
    private val giftSubscriptionButton: Button? by bindView(R.id.gift_subscription_button)

    private val subscribeListitem1Box: View? by bindView(R.id.subscribe_listitem1_box)
    private val subscribeListitem2Box: View? by bindView(R.id.subscribe_listitem2_box)
    private val subscribeListitem3Box: View? by bindView(R.id.subscribe_listitem3_box)
    private val subscribeListitem4Box: View? by bindView(R.id.subscribe_listitem4_box)

    private val subscribeListitem1Button: ImageView? by bindView(R.id.subscribe_listitem1_expand)
    private val subscribeListitem2Button: ImageView? by bindView(R.id.subscribe_listitem2_expand)
    private val subscribeListitem3Button: ImageView? by bindView(R.id.subscribe_listitem3_expand)
    private val subscribeListitem4Button: ImageView? by bindView(R.id.subscribe_listitem4_expand)

    private val subscribeListItem1Description: TextView? by bindView(R.id.subscribe_listitem1_description)
    private val subscribeListItem2Description: TextView? by bindView(R.id.subscribe_listitem2_description)
    private val subscribeListItem3Description: TextView? by bindView(R.id.subscribe_listitem3_description)
    private val subscribeListItem4Description: TextView? by bindView(R.id.subscribe_listitem4_description)

    private val loadingIndicator: ProgressBar? by bindView(R.id.loadingIndicator)
    private val subscriptionOptions: View? by bindView(R.id.subscriptionOptions)

    private val subscription1MonthView: SubscriptionOptionView? by bindView(R.id.subscription1month)
    private val subscription3MonthView: SubscriptionOptionView? by bindView(R.id.subscription3month)
    private val subscription6MonthView: SubscriptionOptionView? by bindView(R.id.subscription6month)
    private val subscription12MonthView: SubscriptionOptionView? by bindView(R.id.subscription12month)

    private val subscriptionButton: Button? by bindOptionalView(R.id.subscribeButton)
    private val subscriptionDetailsView: SubscriptionDetailsView? by bindView(R.id.subscriptionDetails)
    private val subscribeBenefitsTitle: TextView? by bindView(R.id.subscribeBenefitsTitle)
    private val supportTextView: TextView? by bindView(R.id.supportTextView)

    private var selectedSubscriptionSku: Sku? = null
    private var skus: List<Sku> = emptyList()

    private var listener: GemPurchaseActivity? = null
    private var billingRequests: BillingRequests? = null

    private var user: User? = null
    private var hasLoadedSubscriptionOptions: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        fetchUser(null)

        return inflater.inflate(R.layout.fragment_subscription, container, false)
    }

    @Subscribe
    fun fetchUser(event: UserSubscribedEvent?) {
        compositeSubscription.add(userRepository.retrieveUser(false).subscribe(Consumer { this.setUser(it) }, RxErrorHandler.handleEmptyError()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscriptionOptions?.visibility = View.GONE
        subscriptionDetailsView?.visibility = View.GONE

        giftOneGetOneButton?.setOnClickListener { showGiftSubscriptionDialog() }
        giftSubscriptionButton?.setOnClickListener { showGiftSubscriptionDialog() }

        this.subscription1MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription1Month) })
        this.subscription3MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription3Month) })
        this.subscription6MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription6Month) })
        this.subscription12MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription12Month) })

        this.subscribeListitem1Box?.setOnClickListener { toggleDescriptionView(this.subscribeListitem1Button, this.subscribeListItem1Description) }
        this.subscribeListitem2Box?.setOnClickListener { toggleDescriptionView(this.subscribeListitem2Button, this.subscribeListItem2Description) }
        this.subscribeListitem3Box?.setOnClickListener { toggleDescriptionView(this.subscribeListitem3Button, this.subscribeListItem3Description) }
        this.subscribeListitem4Box?.setOnClickListener { toggleDescriptionView(this.subscribeListitem4Button, this.subscribeListItem4Description) }

        val heartDrawable = BitmapDrawable(resources, HabiticaIconsHelper.imageOfHeartLarge())
        supportTextView?.setCompoundDrawables(null, heartDrawable, null, null)

        subscribeButton.setOnClickListener { subscribeUser() }

        giftOneGetOneContainer?.isVisible = appConfigManager.enableGiftOneGetOne()
    }

    private fun toggleDescriptionView(button: ImageView?, descriptionView: TextView?) {
        if (descriptionView?.visibility == View.VISIBLE) {
            descriptionView.visibility = View.GONE
            button?.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        } else {
            descriptionView?.visibility = View.VISIBLE
            button?.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun setupCheckout() {
        val checkout = listener?.activityCheckout
        if (checkout != null) {
            val inventory = checkout.makeInventory()

            inventory.load(Inventory.Request.create()
                    .loadAllPurchases().loadSkus(ProductTypes.SUBSCRIPTION, PurchaseTypes.allSubscriptionTypes)
            ) { products ->
                val subscriptions = products.get(ProductTypes.SUBSCRIPTION)

                skus = subscriptions.skus

                for (sku in skus) {
                    updateButtonLabel(sku, sku.price, subscriptions)
                }
                selectSubscription(PurchaseTypes.Subscription1Month)
                hasLoadedSubscriptionOptions = true
                updateSubscriptionInfo()
            }
        }
    }

    private fun updateButtonLabel(sku: Sku, price: String, subscriptions: Inventory.Product) {
        val matchingView = buttonForSku(sku)
        if (matchingView != null) {
            matchingView.setPriceText(price)
            matchingView.sku = sku.id.code
            matchingView.setIsPurchased(subscriptions.isPurchased(sku))
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
        if (this.selectedSubscriptionSku != null) {
            val oldButton = buttonForSku(this.selectedSubscriptionSku)
            oldButton?.setIsPurchased(false)
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
            PurchaseTypes.Subscription1Month -> subscription1MonthView
            PurchaseTypes.Subscription3Month -> subscription3MonthView
            PurchaseTypes.Subscription6Month -> subscription6MonthView
            PurchaseTypes.Subscription12Month -> subscription12MonthView
            else -> null
        }
    }

    override fun setListener(listener: GemPurchaseActivity) {
        this.listener = listener
    }

    override fun setBillingRequests(billingRequests: BillingRequests?) {
        this.billingRequests = billingRequests
    }

    private fun purchaseSubscription() {
        selectedSubscriptionSku?.id?.code?.let { code ->
            billingRequests?.isPurchased(ProductTypes.SUBSCRIPTION, code, object : RequestListener<Boolean> {
                override fun onSuccess(aBoolean: Boolean) {
                    if (!aBoolean) {
                        // no current product exist
                        val checkout = listener?.activityCheckout
                        checkout?.let {
                            billingRequests?.purchase(ProductTypes.SUBSCRIPTION, code, null, it.purchaseFlow)
                        }
                    }
                }

                override fun onError(i: Int, e: Exception) {
                    crashlyticsProxy.fabricLogE("Purchase", "Error", e)
                }
            })
        }
    }

    fun setUser(newUser: User) {
        user = newUser
        this.updateSubscriptionInfo()
    }

    private fun updateSubscriptionInfo() {
        if (user != null) {
            val plan = user?.purchased?.plan
            var isSubscribed = false
            if (plan != null) {
                if (plan.isActive) {
                    isSubscribed = true
                }
            }

            if (this.subscriptionDetailsView == null) {
                return
            }

            if (isSubscribed) {
                this.subscriptionDetailsView?.visibility = View.VISIBLE
                plan?.let { this.subscriptionDetailsView?.setPlan(it) }
                this.subscribeBenefitsTitle?.setText(R.string.subscribe_prompt_thanks)
                this.subscriptionOptions?.visibility = View.GONE
            } else {
                if (!hasLoadedSubscriptionOptions) {
                    return
                }
                this.subscriptionOptions?.visibility = View.VISIBLE
                this.subscriptionDetailsView?.visibility = View.GONE
            }
            this.loadingIndicator?.visibility = View.GONE
        }
    }

    private fun subscribeUser() {
        purchaseSubscription()
    }

    private fun showGiftSubscriptionDialog() {
        val chooseRecipientDialogView = this.activity?.layoutInflater?.inflate(R.layout.dialog_choose_message_recipient, null)

        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.gift_title))
            alert.addButton(getString(R.string.action_continue), true) { _, _ ->
                        val usernameEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                        val intent = Intent(thisActivity, GiftIAPActivity::class.java).apply {
                            putExtra("username", usernameEditText?.text.toString())
                        }
                        startActivity(intent)
                    }
            alert.addCancelButton() { dialog, _ ->
                        thisActivity.dismissKeyboard()
                    }
            alert.setAdditionalContentView(chooseRecipientDialogView)
            alert.show()
        }
    }
}
