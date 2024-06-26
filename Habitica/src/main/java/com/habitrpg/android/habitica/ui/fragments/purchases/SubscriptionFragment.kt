package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.GiftSubscriptionActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.promo.BirthdayBanner
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import com.habitrpg.common.habitica.theme.HabiticaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionFragment : BaseFragment<FragmentSubscriptionBinding>() {
    override var binding: FragmentSubscriptionBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSubscriptionBinding {
        return FragmentSubscriptionBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private var selectedSubscriptionSku: ProductDetails? = null
    private var skus: List<ProductDetails> = emptyList()

    private var user: User? = null
    private var hasLoadedSubscriptionOptions: Boolean = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.subscriptionOptions?.visibility = View.GONE
        binding?.subscriptionDetails?.visibility = View.GONE
        binding?.subscriptionDetails?.onShowSubscriptionOptions = { showSubscriptionOptions() }

        binding?.giftSubscriptionButton?.setOnClickListener {
            context?.let { context ->
                showGiftSubscriptionDialog(
                    context
                )
            }
        }

        binding?.subscribeButton?.setOnClickListener { purchaseSubscription() }

        lifecycleScope.launchCatching {
            userRepository.getUser().collect { user ->
                user?.let { setUser(it) }
            }
        }

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

        val birthdayEventEnd = appConfigManager.getBirthdayEvent()?.end
        if (birthdayEventEnd != null) {
            binding?.promoComposeView?.setContent {
                HabiticaTheme {
                    BirthdayBanner(
                        endDate = birthdayEventEnd,
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clip(HabiticaTheme.shapes.medium)
                            .padding(bottom = 10.dp)
                    )
                }
            }
            binding?.promoComposeView?.isVisible = true
        }

        binding?.refreshLayout?.setOnRefreshListener { refresh() }

        binding?.subscriptionDisclaimerView?.setMarkdown("Once we’ve confirmed your purchase, the payment will be charged to your Google Account.\n\nSubscriptions automatically renew unless auto-renewal is turned off at least 24-hours before the end of the current period. If you have an active subscription, your account will be charged for renewal within 24-hours prior to the end of your current subscription period and you will be charged the same price you initially paid.\n\nBy continuing you accept the [Terms of Use](https://habitica.com/static/terms) and [Privacy Policy](https://habitica.com/static/privacy).")

        Analytics.sendNavigationEvent("subscription screen")
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchCatching {
            purchaseHandler.queryPurchases()
        }
        refresh()
        loadInventory()
    }

    private fun refresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(withTasks = false, forced = true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun loadInventory() {
        CoroutineScope(Dispatchers.IO).launchCatching {
            val subscriptions = purchaseHandler.getAllSubscriptionProducts()
            skus = subscriptions
            withContext(Dispatchers.Main) {
                for (sku in subscriptions) {
                    updateButtonLabel(
                        sku,
                        sku.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                            ?: ""
                    )
                }
                subscriptions.minByOrNull {
                    it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros
                        ?: 0
                }?.let { selectSubscription(it) }
                hasLoadedSubscriptionOptions = true
                updateSubscriptionInfo()
            }
        }
    }

    private fun updateButtonLabel(
        sku: ProductDetails,
        price: String
    ) {
        val matchingView = buttonForSku(sku)
        if (matchingView != null) {
            matchingView.setPriceText(price)
            matchingView.sku = sku.productId
            matchingView.setOnPurchaseClickListener {
                selectSubscription(sku)
            }
        }
    }

    private fun selectSubscription(sku: ProductDetails) {
        if (this.selectedSubscriptionSku != null) {
            val oldButton = buttonForSku(this.selectedSubscriptionSku)
            oldButton?.setIsSelected(false)
        }
        this.selectedSubscriptionSku = sku
        val subscriptionOptionButton = buttonForSku(this.selectedSubscriptionSku)
        subscriptionOptionButton?.setIsSelected(true)
        if (binding?.subscribeButton != null) {
            binding?.subscribeButton?.isEnabled = true
        }
    }

    private fun buttonForSku(sku: ProductDetails?): SubscriptionOptionView? {
        return buttonForSku(sku?.productId)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.SUBSCRIPTION_1_MONTH -> binding?.subscription1month
            PurchaseTypes.SUBSCRIPTION_3_MONTH -> binding?.subscription3month
            PurchaseTypes.SUBSCRIPTION_6_MONTH -> binding?.subscription6month
            PurchaseTypes.SUBSCRIPTION_12_MONTH -> binding?.subscription12month
            else -> null
        }
    }

    private fun purchaseSubscription() {
        selectedSubscriptionSku?.let { sku ->
            activity?.let {
                purchaseHandler.purchase(it, sku)
            }
        }
    }

    fun setUser(newUser: User) {
        user = newUser
        this.updateSubscriptionInfo()
        checkIfNeedsCancellation()
    }

    private fun updateSubscriptionInfo() {
        if (hasLoadedSubscriptionOptions) {
            binding?.subscriptionOptions?.visibility = View.VISIBLE
            binding?.loadingIndicator?.visibility = View.GONE
        }
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
                binding?.subscriptionDetails?.visibility = View.GONE
                binding?.subscribeBenefitsTitle?.setText(R.string.subscribe_prompt)
            }
            binding?.loadingIndicator?.visibility = View.GONE
        }
    }

    private fun checkIfNeedsCancellation() {
        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
            val newestSubscription = purchaseHandler.checkForSubscription()
            if (user?.purchased?.plan?.paymentMethod == "Google" &&
                user?.purchased?.plan?.isActive == true &&
                user?.purchased?.plan?.dateTerminated == null &&
                (newestSubscription?.isAutoRenewing != true)
            ) {
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    purchaseHandler.cancelSubscription()
                }
            }
        }
    }

    private fun showSubscriptionOptions() {
        binding?.subscriptionOptions?.visibility = View.VISIBLE
        binding?.subscriptionOptions?.postDelayed(
            {
                binding?.scrollView?.smoothScrollTo(0, binding?.subscriptionOptions?.top ?: 0)
            },
            500
        )
    }

    companion object {
        fun showGiftSubscriptionDialog(context: Context) {
            val chooseRecipientDialogView =
                context.layoutInflater.inflate(R.layout.dialog_choose_message_recipient, null)

            val alert = HabiticaAlertDialog(context)
            alert.setTitle(context.getString(R.string.gift_title))
            alert.addButton(context.getString(R.string.action_continue), true) { _, _ ->
                val usernameEditText =
                    chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                val intent =
                    Intent(context, GiftSubscriptionActivity::class.java).apply {
                        putExtra("username", usernameEditText?.text.toString())
                    }
                context.startActivity(intent)
            }
            alert.addCancelButton { _, _ ->
                // context.dismissKeyboard()
            }
            alert.setAdditionalContentView(chooseRecipientDialogView)
            alert.show()
        }
    }
}
