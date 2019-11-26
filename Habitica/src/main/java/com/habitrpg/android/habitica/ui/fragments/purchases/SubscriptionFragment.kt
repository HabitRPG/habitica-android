package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
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
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.Sku
import javax.inject.Inject

class SubscriptionFragment : BaseFragment(), GemPurchaseActivity.CheckoutFragment {

    private lateinit var binding: FragmentSubscriptionBinding
    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager
    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private var selectedSubscriptionSku: Sku? = null
    private var skus: List<Sku> = emptyList()

    private var purchaseHandler: PurchaseHandler? = null

    private var user: User? = null
    private var hasLoadedSubscriptionOptions: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fetchUser(null)
        binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Subscribe
    fun fetchUser(event: UserSubscribedEvent?) {
        compositeSubscription.add(userRepository.retrieveUser(withTasks = false, forced = true).subscribe(Consumer { this.setUser(it) }, RxErrorHandler.handleEmptyError()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.subscriptionOptions.visibility = View.GONE
        binding.subscriptionDetails.visibility = View.GONE
        binding.subscriptionDetails.onShowSubscriptionOptions = { showSubscriptionOptions() }

        binding.giftSubscriptionContainer.setOnClickListener { showGiftSubscriptionDialog() }
        binding.giftSubscriptionButton.setOnClickListener { showGiftSubscriptionDialog() }

        binding.subscription1month.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription1Month) })
        binding.subscription3month.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription3Month) })
        binding.subscription6month.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription6Month) })
        binding.subscription12month.setOnPurchaseClickListener(View.OnClickListener { selectSubscription(PurchaseTypes.Subscription12Month) })

        binding.subscribeButton.setOnClickListener { subscribeUser() }

        binding.giftSubscriptionContainer?.isVisible = appConfigManager.enableGiftOneGetOne()

        if (appConfigManager.useNewMysteryBenefits()) {
            compositeSubscription.add(inventoryRepository.getLatestMysteryItem().subscribe(Consumer {
                DataBindingUtils.loadImage(binding.subBenefitsMysteryItemIcon, "shop_set_mystery_${it.key?.split("_")?.last()}")
                binding.subBenefitsMysteryItemText.text = context?.getString(R.string.subscribe_listitem3_description_new, it.text)
            }, RxErrorHandler.handleEmptyError()))
        }
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
        if (binding.subscribeButton != null) {
            binding.subscribeButton?.isEnabled = true
        }
    }

    private fun buttonForSku(sku: Sku?): SubscriptionOptionView? {
        return buttonForSku(sku?.id?.code)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.Subscription1Month -> binding.subscription1month
            PurchaseTypes.Subscription3Month -> binding.subscription3month
            PurchaseTypes.Subscription6Month -> binding.subscription6month
            PurchaseTypes.Subscription12Month -> binding.subscription12month
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

            if (binding.subscriptionDetails == null) {
                return
            }

            if (isSubscribed) {
                binding.headerImageView?.setImageResource(R.drawable.subscriber_header)
                binding.subscriptionDetails.visibility = View.VISIBLE
                binding.subscriptionDetails.currentUserID = user?.id
                user?.purchased?.plan?.let { binding.subscriptionDetails.setPlan(it) }
                binding.subscribeBenefitsTitle.setText(R.string.subscribe_prompt_thanks)
                binding.subscriptionOptions.visibility = View.GONE
            } else {
                binding.headerImageView.setImageResource(R.drawable.subscribe_header)
                if (!hasLoadedSubscriptionOptions) {
                    return
                }
                binding.subscriptionOptions.visibility = View.VISIBLE
                binding.subscriptionDetails.visibility = View.GONE
                binding.subscribeBenefitsTitle.setText(R.string.subscribe_prompt)
            }
            binding.loadingIndicator.visibility = View.GONE
        }
    }

    private fun showSubscriptionOptions() {
        binding.subscriptionOptions.visibility = View.VISIBLE
        binding.subscriptionOptions.postDelayed({
            binding.scrollView.smoothScrollTo(0, binding.subscriptionOptions.top ?: 0)
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
