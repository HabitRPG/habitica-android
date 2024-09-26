package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Context
import android.content.Intent
import android.net.Uri
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
        container: ViewGroup?,
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
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.content?.subscriptionOptions?.visibility = View.GONE
        binding?.content?.subscriptionDetails?.visibility = View.GONE
        binding?.content?.subscriptionDetails?.onShowSubscriptionOptions = { showSubscriptionOptions() }

        binding?.content?.giftSegmentSubscribed?.giftSubscriptionButton?.setOnClickListener {
            context?.let { context ->
                showGiftSubscriptionDialog(
                    context,
                )
            }
        }
        binding?.content?.giftSegmentUnsubscribed?.giftSubscriptionButton?.setOnClickListener {
            context?.let { context ->
                showGiftSubscriptionDialog(
                    context,
                )
            }
        }

        binding?.content?.subscribeButton?.setOnClickListener { purchaseSubscription() }

        binding?.content?.visitHabiticaWebsiteButton?.setOnClickListener {
            val url = context?.getString(R.string.base_url) + "/"
            context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

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
            binding?.content?.promoBanner?.setOnClickListener {
                val fragment = PromoInfoFragment()
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment as Fragment)
                    .commit()
            }
        } else {
            binding?.content?.promoBanner?.visibility = View.GONE
        }

        val birthdayEventEnd = appConfigManager.getBirthdayEvent()?.end
        if (birthdayEventEnd != null) {
            binding?.content?.promoComposeView?.setContent {
                HabiticaTheme {
                    BirthdayBanner(
                        endDate = birthdayEventEnd,
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clip(HabiticaTheme.shapes.medium)
                            .padding(bottom = 10.dp),
                    )
                }
            }
            binding?.content?.promoComposeView?.isVisible = true
        }

        binding?.refreshLayout?.setOnRefreshListener { refresh() }

        binding?.content?.subscriptionDisclaimerView?.setMarkdown("Once we’ve confirmed your purchase, the payment will be charged to your Google Account.\n\nSubscriptions automatically renew unless auto-renewal is turned off at least 24-hours before the end of the current period. If you have an active subscription, your account will be charged for renewal within 24-hours prior to the end of your current subscription period and you will be charged the same price you initially paid.\n\nBy continuing you accept the [Terms of Use](https://habitica.com/static/terms) and [Privacy Policy](https://habitica.com/static/privacy).")

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
                binding?.content?.loadingIndicator?.visibility = View.GONE
                if (subscriptions.isEmpty()) {
                    if (user?.isSubscribed != true) {
                        binding?.content?.noBillingSubscriptions?.visibility = View.VISIBLE
                        binding?.content?.visitHabiticaWebsiteButton?.visibility = View.VISIBLE
                    }
                    return@withContext
                }
                binding?.content?.noBillingSubscriptions?.visibility = View.GONE
                binding?.content?.visitHabiticaWebsiteButton?.visibility = View.GONE
                for (sku in subscriptions) {
                    updateButtonLabel(
                        sku,
                        sku.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                            ?: "",
                    )
                }
                subscriptions.maxByOrNull {
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
        price: String,
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
        if (binding?.content?.subscribeButton != null) {
            binding?.content?.subscribeButton?.isEnabled = true
        }
    }

    private fun buttonForSku(sku: ProductDetails?): SubscriptionOptionView? {
        return buttonForSku(sku?.productId)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.SUBSCRIPTION_1_MONTH -> binding?.content?.subscription1month
            PurchaseTypes.SUBSCRIPTION_3_MONTH -> binding?.content?.subscription3month
            PurchaseTypes.SUBSCRIPTION_6_MONTH -> binding?.content?.subscription6month
            PurchaseTypes.SUBSCRIPTION_12_MONTH -> binding?.content?.subscription12month
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
            binding?.content?.subscriptionOptions?.visibility = View.VISIBLE
            binding?.content?.loadingIndicator?.visibility = View.GONE
        }
        if (user != null) {
            val isSubscribed = user?.isSubscribed ?: false

            if (binding?.content?.subscriptionDetails == null) {
                return
            }

            if (isSubscribed) {
                binding?.content?.headerImageView?.setImageResource(R.drawable.subscriber_banner_dark)
                binding?.content?.subscriptionDetails?.visibility = View.VISIBLE
                binding?.content?.subscriptionDetails?.currentUserID = user?.id
                user?.purchased?.plan?.let { binding?.content?.subscriptionDetails?.setPlan(it) }
                binding?.content?.subscriptionOptions?.visibility = View.GONE
                binding?.content?.giftSegmentUnsubscribed?.root?.visibility = View.GONE
                binding?.content?.giftSegmentSubscribed?.root?.visibility = View.VISIBLE
                binding?.content?.subscribeBenefitsTitle?.visibility = View.GONE
                binding?.content?.subscriptionDisclaimerView?.visibility = View.GONE
            } else {
                binding?.content?.headerImageView?.setImageResource(R.drawable.subscribe_header_dark)
                if (!hasLoadedSubscriptionOptions) {
                    return
                }
                binding?.content?.subscriptionDetails?.visibility = View.GONE
                binding?.content?.subscribeBenefitsTitle?.setText(R.string.subscribe_prompt)
                binding?.content?.subscribeBenefitsTitle?.visibility = View.VISIBLE
                binding?.content?.subscribeBenefitsFooter?.visibility = View.GONE
                binding?.content?.giftSegmentSubscribed?.root?.visibility = View.GONE
                binding?.content?.giftSegmentUnsubscribed?.root?.visibility = View.VISIBLE
                binding?.content?.subscriptionDisclaimerView?.visibility = View.VISIBLE

                binding?.content?.subscription12month?.showHourglassPromo(user?.purchased?.plan?.isEligableForHourglassPromo == true)

            }
            binding?.content?.loadingIndicator?.visibility = View.GONE
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
        binding?.content?.subscriptionOptions?.visibility = View.VISIBLE
        binding?.content?.subscriptionOptions?.postDelayed(
            {
                binding?.content?.scrollView?.smoothScrollTo(0, binding?.content?.subscriptionOptions?.top ?: 0)
            },
            500,
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
