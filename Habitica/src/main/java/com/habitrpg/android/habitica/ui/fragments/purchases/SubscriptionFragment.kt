package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.events.UserSubscribedEvent
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.GiftSubscriptionActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import javax.inject.Inject

class SubscriptionFragment : BaseFragment<FragmentSubscriptionBinding>(), GemPurchaseActivity.CheckoutFragment {

    override var binding: FragmentSubscriptionBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSubscriptionBinding {
        return FragmentSubscriptionBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var apiClient: ApiClient

    private var selectedSubscriptionSku: Sku? = null
    private var skus: List<Sku> = emptyList()

    private var purchaseHandler: PurchaseHandler? = null

    private var user: User? = null
    private var hasLoadedSubscriptionOptions: Boolean = false
    private var purchasedSubscription: Purchase? = null

    @Subscribe
    fun fetchUser(event: UserSubscribedEvent?) {
        refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.subscriptionOptions?.visibility = View.GONE
        binding?.subscriptionDetails?.visibility = View.GONE
        binding?.subscriptionDetails?.onShowSubscriptionOptions = { showSubscriptionOptions() }

        binding?.giftSubscriptionButton?.setOnClickListener { context?.let { context -> showGiftSubscriptionDialog(context, appConfigManager.activePromo()?.identifier == "g1g1") } }

        binding?.subscription1month?.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription1Month) }
        binding?.subscription3month?.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription3Month) }
        binding?.subscription6month?.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription6Month) }
        binding?.subscription12month?.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription12Month) }

        binding?.subscribeButton?.setOnClickListener { subscribeUser() }

        val promo = appConfigManager.activePromo()
        if (promo != null) {
            binding?.let {
                promo.configurePurchaseBanner(it)
            }
            binding?.promoBanner?.setOnClickListener {
                val fragment = PromoInfoFragment()
                parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment as Fragment)
                        .commit()
            }
        } else {
            binding?.promoBanner?.visibility = View.GONE
        }

        binding?.refreshLayout?.setOnRefreshListener { refresh() }

        compositeSubscription.add(inventoryRepository.getLatestMysteryItem().subscribe({
            DataBindingUtils.loadImage(binding?.subBenefitsMysteryItemIcon, "shop_set_mystery_${it.key?.split("_")?.last()}")
            binding?.subBenefitsMysteryItemText?.text = context?.getString(R.string.subscribe_listitem3_description_new, it.text)
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        compositeSubscription.add(userRepository.retrieveUser(withTasks = false, forced = true).subscribe({
            this.setUser(it)
            binding?.refreshLayout?.isRefreshing = false
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun setupCheckout() {
        purchaseHandler?.getAllSubscriptionProducts { subscriptions ->
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
        if (binding?.subscribeButton != null) {
            binding?.subscribeButton?.isEnabled = true
        }
    }

    private fun buttonForSku(sku: Sku?): SubscriptionOptionView? {
        return buttonForSku(sku?.id?.code)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.Subscription1Month -> binding?.subscription1month
            PurchaseTypes.Subscription3Month -> binding?.subscription3month
            PurchaseTypes.Subscription6Month -> binding?.subscription6month
            PurchaseTypes.Subscription12Month -> binding?.subscription12month
            else -> null
        }
    }

    override fun setPurchaseHandler(handler: PurchaseHandler?) {
        this.purchaseHandler = handler

        handler?.checkForSubscription {
            purchasedSubscription = it
            checkIfNeedsCancellation()
        }
    }

    private fun purchaseSubscription() {
        selectedSubscriptionSku?.let { sku ->
            purchaseHandler?.purchaseSubscription(sku) {
                fetchUser(null)
                binding?.scrollView?.smoothScrollTo(0, 0)
            }
        }
    }

    fun setUser(newUser: User) {
        user = newUser
        this.updateSubscriptionInfo()
        purchaseHandler?.checkForSubscription {
            purchasedSubscription = it
            checkIfNeedsCancellation()
        }
    }

    private fun updateSubscriptionInfo() {
        if (user != null) {
            val isSubscribed = user?.isSubscribed ?: false

            if (binding?.subscriptionDetails == null) {
                return
            }

            if (isSubscribed) {
                if (context?.isUsingNightModeResources() == true) {
                    binding?.headerImageView?.setImageResource(R.drawable.subscriber_banner_dark)
                } else {
                    binding?.headerImageView?.setImageResource(R.drawable.subscriber_header)
                }
                binding?.subscriptionDetails?.visibility = View.VISIBLE
                binding?.subscriptionDetails?.currentUserID = user?.id
                user?.purchased?.plan?.let { binding?.subscriptionDetails?.setPlan(it) }
                binding?.subscribeBenefitsTitle?.setText(R.string.subscribe_prompt_thanks)
                binding?.subscriptionOptions?.visibility = View.GONE
            } else {
                if (context?.isUsingNightModeResources() == true) {
                    binding?.headerImageView?.setImageResource(R.drawable.subscribe_header_dark)
                } else {
                    binding?.headerImageView?.setImageResource(R.drawable.subscribe_header)
                }
                if (!hasLoadedSubscriptionOptions) {
                    return
                }
                binding?.subscriptionOptions?.visibility = View.VISIBLE
                binding?.subscriptionDetails?.visibility = View.GONE
                binding?.subscribeBenefitsTitle?.setText(R.string.subscribe_prompt)
            }
            binding?.loadingIndicator?.visibility = View.GONE
        }
    }

    private fun checkIfNeedsCancellation() {
        if (user?.purchased?.plan?.paymentMethod == "Google" &&
                user?.purchased?.plan?.isActive == true &&
                (purchasedSubscription?.autoRenewing == false ||purchasedSubscription == null)) {
            compositeSubscription.add(apiClient.cancelSubscription().subscribe({
                refresh()
            }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun showSubscriptionOptions() {
        binding?.subscriptionOptions?.visibility = View.VISIBLE
        binding?.subscriptionOptions?.postDelayed({
            binding?.scrollView?.smoothScrollTo(0, binding?.subscriptionOptions?.top ?: 0)
        }, 500)
    }

    private fun subscribeUser() {
        purchaseSubscription()
    }

    companion object {
        fun showGiftSubscriptionDialog(context: Context, iSG1G1: Boolean) {
            val chooseRecipientDialogView = context.layoutInflater.inflate(R.layout.dialog_choose_message_recipient, null)

                val alert = HabiticaAlertDialog(context)
                alert.setTitle(context.getString(R.string.gift_title))
                alert.addButton(context.getString(R.string.action_continue), true) { _, _ ->
                    val usernameEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                    val intent = Intent(context, GiftSubscriptionActivity::class.java).apply {
                        putExtra("username", usernameEditText?.text.toString())
                    }
                    context.startActivity(intent)
                }
                alert.addCancelButton { _, _ ->
                   //context.dismissKeyboard()
                }
                alert.setAdditionalContentView(chooseRecipientDialogView)
                alert.show()
        }
    }
}
