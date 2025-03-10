package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityGiftSubscriptionBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject

@AndroidEntryPoint
class GiftSubscriptionActivity : PurchaseActivity() {
    private lateinit var binding: ActivityGiftSubscriptionBinding

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private var giftedUsername: String? = null
    private var giftedUserID: String? = null

    private var selectedSubscriptionSku: ProductDetails? = null
    private var skus: List<ProductDetails> = emptyList()

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift_subscription
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityGiftSubscriptionBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.gift_subscription)
        setupToolbar(binding.toolbar, Color.WHITE, ContextCompat.getColor(this, R.color.brand_300))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        giftedUserID = intent.getStringExtra("userID")
        giftedUsername = intent.getStringExtra("username")
        if (giftedUserID.isNullOrBlank()) {
            try {
                giftedUserID = navArgs<GiftSubscriptionActivityArgs>().value.userID
            } catch (_: InvocationTargetException) {
                // user ID wasn't passed as nav arg
            }
        }
        if (giftedUsername.isNullOrBlank()) {
            try {
                giftedUsername = navArgs<GiftSubscriptionActivityArgs>().value.username
            } catch (_: InvocationTargetException) {
                // username wasn't passed as nav arg
            }
        }

        if (giftedUsername.isNullOrBlank() && giftedUserID.isNullOrBlank()) {
            showMemberLoadingErrorDialog()
        }

        if (giftedUsername?.isNotBlank() == true) {
            binding.usernameTextView.text = "@$giftedUsername"
        }

        binding.subscriptionButton.setOnClickListener {
            selectedSubscriptionSku?.let { sku -> purchaseSubscription(sku) }
        }
        lifecycleScope.launch(
            ExceptionHandler.coroutine {
                showMemberLoadingErrorDialog()
            }
        ) {
            val member = socialRepository.retrieveMember(giftedUsername ?: giftedUserID)
            if (member == null) {
                showMemberLoadingErrorDialog()
                return@launch
            }
            binding.avatarView.setAvatar(member)
            binding.displayNameTextView.text = member.profile?.name
            binding.usernameTextView.text = "@${member.username}"
            giftedUserID = member.id
            giftedUsername = member.username
        }

        if (appConfigManager.activePromo()?.identifier == "g1g1") {
            binding.giftSubscriptionContainer.visibility = View.VISIBLE
            binding.hillsBg.setImageResource(R.drawable.footer_hills_g1g1)
        } else {
            binding.giftSubscriptionContainer.visibility = View.GONE
            binding.hillsBg.setImageResource(R.drawable.footer_hills)
        }
    }

    override fun onResume() {
        super.onResume()
        window.updateStatusBarColor(ContextCompat.getColor(this, R.color.brand_300), false)
    }

    private fun showMemberLoadingErrorDialog() {
        val dialog = HabiticaAlertDialog(this@GiftSubscriptionActivity)
        dialog.setTitle(R.string.error_loading_member)
        dialog.setMessage(R.string.error_loading_member_body)
        dialog.addCloseButton(isPrimary = true) { _, _ -> finish() }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
            val subscriptions = purchaseHandler.getAllGiftSubscriptionProducts()
            skus = subscriptions
            withContext(Dispatchers.Main) {
                for (sku in skus) {
                    updateButtonLabel(sku)
                }
                if (selectedSubscriptionSku == null) {
                    skus.maxByOrNull { it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0 }
                        ?.let { selectSubscription(it) }
                }
            }
        }
    }

    private fun updateButtonLabel(sku: ProductDetails) {
        val matchingView = buttonForSku(sku)
        if (matchingView != null) {
            matchingView.setPriceText(sku.oneTimePurchaseOfferDetails?.formattedPrice ?: "")
            matchingView.sku = sku.productId
            matchingView.setOnPurchaseClickListener { selectSubscription(sku) }
        }
    }

    private fun selectSubscription(sku: ProductDetails) {
        for (thisSku in skus) {
            buttonForSku(thisSku)?.setIsSelected(false)
        }
        this.selectedSubscriptionSku = sku
        val subscriptionOptionButton = buttonForSku(this.selectedSubscriptionSku)
        subscriptionOptionButton?.setIsSelected(true)
        binding.subscriptionButton.isEnabled = true
    }

    private fun buttonForSku(sku: ProductDetails?): SubscriptionOptionView? {
        return buttonForSku(sku?.productId)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.SUBSCRIPTION_1_MONTH_NORENEW -> binding.subscription1MonthView
            PurchaseTypes.SUBSCRIPTION_3_MONTH_NORENEW -> binding.subscription3MonthView
            PurchaseTypes.SUBSCRIPTION_6_MONTH_NORENEW -> binding.subscription6MonthView
            PurchaseTypes.SUBSCRIPTION_12_MONTH_NORENEW -> binding.subscription12MonthView
            else -> null
        }
    }

    private fun purchaseSubscription(sku: ProductDetails) {
        giftedUserID?.let { id ->
            if (id.isEmpty()) {
                return
            }
            PurchaseHandler.addGift(sku.productId, id, giftedUsername ?: id)
            purchaseHandler.purchase(this, sku)
        }
    }
}
