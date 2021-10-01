package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.navArgs
import com.habitrpg.android.habitica.HabiticaPurchaseVerifier
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityGiftSubscriptionBinding
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.Sku
import javax.inject.Inject

class GiftSubscriptionActivity : BaseActivity() {

    private lateinit var binding: ActivityGiftSubscriptionBinding

    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private var purchaseHandler: PurchaseHandler? = null

    private var giftedUsername: String? = null
    private var giftedUserID: String? = null

    private var selectedSubscriptionSku: Sku? = null
    private var skus: List<Sku> = emptyList()

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift_subscription
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun getContentView(): View {
        binding = ActivityGiftSubscriptionBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.gift_subscription)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        giftedUserID = intent.getStringExtra("userID")
        giftedUsername = intent.getStringExtra("username")
        if (giftedUserID == null && giftedUsername == null) {
            giftedUserID = navArgs<GiftSubscriptionActivityArgs>().value.userID
            giftedUsername = navArgs<GiftSubscriptionActivityArgs>().value.username
        }

        binding.subscriptionButton.setOnClickListener {
            selectedSubscriptionSku?.let { sku -> purchaseSubscription(sku) }
        }

        compositeSubscription.add(
            socialRepository.getMember(giftedUsername ?: giftedUserID).subscribe(
                {
                    binding.avatarView.setAvatar(it)
                    binding.displayNameTextView.username = it.profile?.name
                    binding.displayNameTextView.tier = it.contributor?.level ?: 0
                    binding.usernameTextView.text = "@${it.username}"
                    giftedUserID = it.id
                    giftedUsername = it.username
                },
                RxErrorHandler.handleEmptyError()
            )
        )

        if (appConfigManager.activePromo()?.identifier == "g1g1") {
            binding.giftSubscriptionContainer.visibility = View.VISIBLE
        } else {
            binding.giftSubscriptionContainer.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        purchaseHandler = PurchaseHandler(this, analyticsManager)
        purchaseHandler?.startListening()
        CoroutineScope(Dispatchers.IO).launch {
            val subscriptions = purchaseHandler?.getAllGiftSubscriptionProducts()
            skus = subscriptions?.skus ?: return@launch
            for (sku in skus) {
                updateButtonLabel(sku, sku.price, subscriptions)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.subscription1MonthView.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription1MonthNoRenew) }
        binding.subscription3MonthView.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription3MonthNoRenew) }
        binding.subscription6MonthView.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription6MonthNoRenew) }
        binding.subscription12MonthView.setOnPurchaseClickListener { selectSubscription(PurchaseTypes.Subscription12MonthNoRenew) }
    }

    override fun onStop() {
        purchaseHandler?.stopListening()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        purchaseHandler?.onResult(requestCode, resultCode, data)
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
        binding.subscriptionButton.isEnabled = true
    }

    private fun buttonForSku(sku: Sku?): SubscriptionOptionView? {
        return buttonForSku(sku?.id?.code)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.Subscription1MonthNoRenew -> binding.subscription1MonthView
            PurchaseTypes.Subscription3MonthNoRenew -> binding.subscription3MonthView
            PurchaseTypes.Subscription6MonthNoRenew -> binding.subscription6MonthView
            PurchaseTypes.Subscription12MonthNoRenew -> binding.subscription12MonthView
            else -> null
        }
    }

    private fun purchaseSubscription(sku: Sku) {
        if (giftedUserID?.isNotEmpty() != true) {
            return
        }
        HabiticaPurchaseVerifier.addGift(sku.id.code, giftedUserID)
        purchaseHandler?.purchaseNoRenewSubscription(sku)
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        purchaseHandler?.consumePurchase(event.purchase)
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
        val message = getString(
            if (appConfigManager.activePromo()?.identifier == "g1g1") {
                R.string.gift_confirmation_text_sub_g1g1
            } else {
                R.string.gift_confirmation_text_sub
            },
            giftedUsername, selectedDurationString()
        )
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.gift_confirmation_title)
        alert.setMessage(message)
        alert.addOkButton { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        alert.enqueue()
    }
}
