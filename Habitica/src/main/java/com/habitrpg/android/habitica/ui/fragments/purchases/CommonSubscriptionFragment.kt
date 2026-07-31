package com.habitrpg.android.habitica.ui.fragments.purchases

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionContentBinding
import com.habitrpg.android.habitica.helpers.HabiticaProduct
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.getBaseOfferDetails
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.showAsBottomSheet
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface CommonSubscriptionFragment: LifecycleOwner {
    var userRepository: UserRepository
    var purchaseHandler: PurchaseHandler
    var selectedSubscriptionSku: ProductDetails?
    var skus: List<ProductDetails>
    var user: User?
    var hasLoadedSubscriptionOptions: Boolean

    val content: FragmentSubscriptionContentBinding?

    fun getViewLifecycleOwner(): LifecycleOwner

    fun setupView(activity: Activity) {
        lifecycleScope.launchCatching {
            userRepository.getUser().collect { user ->
                user?.let { updateUser(it) }
            }
        }
        val content = content ?: return
        content.subscriptionOptions.visibility = View.GONE
        content.subscriptionDetails.visibility = View.GONE
        content.subscriptionDetails.onShowSubscriptionOptions = { showSubscriptionOptions() }

        content.subscriptionDetails.onUpdateSubscriptionsTapped = {
            activity.showAsBottomSheet(sheetColor = Color(activity.getColor(R.color.brand_300)), true) {
                ChangeSubscriptionScreen(it)
            }
        }
        content.subscribeButton.setOnClickListener { purchaseSubscription() }

        content.visitHabiticaWebsiteButton.setOnClickListener {
            val url = activity.getString(R.string.base_url) + "/"
            activity.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }

        content.subscriptionDisclaimerView.visibility = View.VISIBLE
        content.subscriptionDisclaimerView.setMarkdown(
            "Once we’ve confirmed your purchase, the payment will be charged to your Google Account.\n\nSubscriptions automatically renew unless auto-renewal is turned off at least 24-hours before the end of the current period. If you have an active subscription, your account will be charged for renewal within 24-hours prior to the end of your current subscription period and you will be charged the same price you initially paid.\n\nBy continuing you accept the [Terms of Use](https://habitica.com/static/terms) and [Privacy Policy](https://habitica.com/static/privacy).",
        )
    }

    fun refresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(false, true)
        }
    }

    fun purchaseSubscription()

    private fun updateUser(newUser: User) {
        user = newUser
        this.updateSubscriptionInfo()
        checkIfNeedsCancellation()
    }

    fun showSubscriptionOptions() {
        content?.subscriptionOptions?.visibility = View.VISIBLE
        content?.subscriptionOptions?.postDelayed(
            {
                content?.nestedScrollView?.smoothScrollTo(0, content?.subscriptionOptions?.top ?: 0)
            },
            500,
        )
    }

    fun loadInventory() {
        getViewLifecycleOwner().lifecycleScope.launchCatching {
            val subscriptions = purchaseHandler.loadSubscriptionProducts()
            skus = subscriptions
            withContext(Dispatchers.Main) {
                content?.loadingIndicator?.visibility = View.GONE
                if (subscriptions.isEmpty()) {
                    if (user?.isSubscribed != true) {
                        content?.noBillingSubscriptions?.visibility = View.VISIBLE
                        content?.visitHabiticaWebsiteButton?.visibility = View.VISIBLE
                    }
                    return@withContext
                }
                content?.noBillingSubscriptions?.visibility = View.GONE
                content?.visitHabiticaWebsiteButton?.visibility = View.GONE
                for (sku in subscriptions) {
                    updateButtonLabel(
                        sku,
                        sku
                            .getBaseOfferDetails()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice
                            ?: "",
                    )
                }
                if (selectedSubscriptionSku == null) {
                    subscriptions
                        .maxByOrNull {
                            it
                                .getBaseOfferDetails()
                                ?.pricingPhases
                                ?.pricingPhaseList
                                ?.firstOrNull()
                                ?.priceAmountMicros
                                ?: 0
                        }?.let { selectSubscription(it) }
                }
                hasLoadedSubscriptionOptions = true
                updateSubscriptionInfo()
            }
        }
    }

    fun updateButtonLabel(
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

    fun buttonForSku(sku: ProductDetails): SubscriptionOptionView? = buttonForSku(sku.productId)

    fun buttonForSku(sku: String): SubscriptionOptionView? =
        when (HabiticaProduct.forSku(sku)) {
            HabiticaProduct.SUBSCRIPTION_1_MONTH -> content?.subscription1month
            HabiticaProduct.SUBSCRIPTION_3_MONTH -> content?.subscription3month
            HabiticaProduct.SUBSCRIPTION_12_MONTH -> content?.subscription12month
            else -> null
        }


    fun selectSubscription(sku: ProductDetails) {
        selectedSubscriptionSku?.let {
            val oldButton = buttonForSku(it)
            oldButton?.setIsSelected(false)
        }
        this.selectedSubscriptionSku = sku
        val subscriptionOptionButton = buttonForSku(sku)
        subscriptionOptionButton?.setIsSelected(true)
        content?.subscribeButton?.isEnabled = true
    }

    fun updateSubscriptionInfo() {
        if (hasLoadedSubscriptionOptions) {
            content?.subscriptionOptions?.visibility = View.VISIBLE
            content?.loadingIndicator?.visibility = View.GONE
        }
        if (user != null) {
            val isSubscribed = user?.isSubscribed ?: false

            if (content?.subscriptionDetails == null) {
                return
            }

            if (isSubscribed) {
                content?.headerImageView?.setImageResource(R.drawable.subscriber_banner_dark)
                content?.subscriptionDetails?.visibility = View.VISIBLE
                content?.subscriptionDetails?.currentUserID = user?.id
                user?.purchased?.plan?.let { content?.subscriptionDetails?.setPlan(it) }
                content?.subscriptionOptions?.visibility = View.GONE
                content
                    ?.giftSegmentUnsubscribed
                    ?.root
                    ?.visibility = View.GONE
                content
                    ?.giftSegmentSubscribed
                    ?.root
                    ?.visibility = View.VISIBLE
                content?.subscribeBenefitsTitle?.visibility = View.GONE
                content?.subscribeBenefitsFooter?.visibility = View.VISIBLE
                content?.subscriptionDisclaimerView?.visibility = View.GONE
                content?.existingGemCapBonusView?.visibility = View.GONE
            } else {
                content?.headerImageView?.setImageResource(R.drawable.subscribe_header_dark)
                if (!hasLoadedSubscriptionOptions) {
                    return
                }
                content?.subscriptionDetails?.visibility = View.GONE
                content?.subscribeBenefitsTitle?.setText(R.string.subscribe_prompt)
                content?.subscribeBenefitsTitle?.visibility = View.VISIBLE
                content?.subscribeBenefitsFooter?.visibility = View.GONE
                content
                    ?.giftSegmentSubscribed
                    ?.root
                    ?.visibility = View.GONE
                content
                    ?.giftSegmentUnsubscribed
                    ?.root
                    ?.visibility = View.VISIBLE
                content?.subscriptionDisclaimerView?.visibility = View.VISIBLE

                val totalGemCap = user?.purchased?.plan?.totalNumberOfGemsAlways ?: 24
                content?.subscription1month?.gemCap = totalGemCap
                content?.subscription3month?.gemCap = totalGemCap
                content?.subscription6month?.gemCap = totalGemCap

                if (totalGemCap > 24) {
                    content?.existingGemCapBonusView?.visibility = View.VISIBLE
                    content?.gemCapExtraLabel?.text = content?.root?.context?.getString(R.string.gem_cap_extra, totalGemCap, 50)
                    content?.extraGemsProgress?.progress = totalGemCap
                } else {
                    content?.existingGemCapBonusView?.visibility = View.GONE
                }

                content?.subscription12month?.showHourglassPromo(user?.purchased?.plan?.isEligableForHourglassPromo == true)
            }
            content?.loadingIndicator?.visibility = View.GONE
        }
    }

    fun checkIfNeedsCancellation() {
        getViewLifecycleOwner().lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val newestSubscription = purchaseHandler.checkForSubscription(false)
            val plan = user?.purchased?.plan
            val sub = HabiticaProduct.forSku(newestSubscription?.products?.firstOrNull() ?: "")
            if (plan?.paymentMethod == "Google" && plan.isActive && plan.dateTerminated == null &&
                newestSubscription?.isAutoRenewing != true
            ) {
                purchaseHandler.cancelSubscription()
            } else if (plan?.paymentMethod == "Google" && plan.isActive && plan.dateTerminated == null &&
                plan.planId != sub?.getSubCode()
            ) {
                purchaseHandler.updateSubscriptionPlan(newestSubscription)
            }
        }
    }
}
