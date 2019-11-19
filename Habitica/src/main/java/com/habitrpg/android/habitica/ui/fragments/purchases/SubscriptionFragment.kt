package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.UserSubscribedEvent
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.GiftSubscriptionActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_subscription.*
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.Sku
import javax.inject.Inject

class SubscriptionFragment : BaseFragment(), GemPurchaseActivity.CheckoutFragment {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private val scrollView: NestedScrollView? by bindView(R.id.scroll_view)

    private val giftOneGetOneContainer: ViewGroup? by bindView(R.id.gift_subscription_container)
    private val giftSubscriptionButton: Button? by bindView(R.id.gift_subscription_button)

    private val headerImageView: ImageView? by bindView(R.id.header_image_view)

    private val loadingIndicator: ProgressBar? by bindOptionalView(R.id.loadingIndicator)
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

    private var purchaseHandler: PurchaseHandler? = null

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
        compositeSubscription.add(userRepository.retrieveUser(false, true).subscribe(Consumer { this.setUser(it) }, RxErrorHandler.handleEmptyError()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        subscriptionOptions?.visibility = View.GONE
        subscriptionDetailsView?.visibility = View.GONE
        subscriptionDetailsView?.onShowSubscriptionOptions = { showSubscriptionOptions() }

        giftOneGetOneContainer?.setOnClickListener { showGiftSubscriptionDialog() }
        giftSubscriptionButton?.setOnClickListener { showGiftSubscriptionDialog() }

        this.subscription1MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription1Month) })
        this.subscription3MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription3Month) })
        this.subscription6MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription6Month) })
        this.subscription12MonthView?.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription12Month) })

        val heartDrawable = BitmapDrawable(resources, HabiticaIconsHelper.imageOfHeartLarge())
        supportTextView?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, heartDrawable)

        subscribeButton.setOnClickListener { subscribeUser() }

        giftOneGetOneContainer?.isVisible = appConfigManager.enableGiftOneGetOne()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun setupCheckout() {
        purchaseHandler?.getAllSubscriptionProducts {subscriptions ->
            this.skus = subscriptions.skus
            for (sku in subscriptions.skus) {
                updateButtonLabel(sku, sku.price, subscriptions)
            }
            selectSubscription(PurchaseTypes.Subscription1Month)
            hasLoadedSubscriptionOptions = true
            updateSubscriptionInfo()
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

    override fun setPurchaseHandler(handler: PurchaseHandler?) {
        this.purchaseHandler = handler
    }

    private fun purchaseSubscription() {
        selectedSubscriptionSku?.let { sku ->
            purchaseHandler?.purchaseSubscription(sku) {
                fetchUser(null)
            }
        }
    }

    fun setUser(newUser: User) {
        user = newUser
        this.updateSubscriptionInfo()
    }

    private fun updateSubscriptionInfo() {
        if (user != null) {
            val isSubscribed = user?.isSubscribed ?: false

            if (subscriptionDetailsView == null) {
                return
            }

            if (isSubscribed) {
                headerImageView?.setImageResource(R.drawable.subscriber_header)
                subscriptionDetailsView?.visibility = View.VISIBLE
                user?.purchased?.plan?.let { this.subscriptionDetailsView?.setPlan(it) }
                subscribeBenefitsTitle?.setText(R.string.subscribe_prompt_thanks)
                subscriptionOptions?.visibility = View.GONE
            } else {
                headerImageView?.setImageResource(R.drawable.subscribe_header)
                if (!hasLoadedSubscriptionOptions) {
                    return
                }
                subscriptionOptions?.visibility = View.VISIBLE
                subscriptionDetailsView?.visibility = View.GONE
            }
            loadingIndicator?.visibility = View.GONE
        }
    }

    private fun showSubscriptionOptions() {
        subscriptionOptions?.visibility = View.VISIBLE
        subscriptionOptions?.postDelayed({
            scrollView?.smoothScrollTo(0, subscriptionOptions?.top ?: 0)
        }, 500)
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
                        val intent = Intent(thisActivity, GiftSubscriptionActivity::class.java).apply {
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
