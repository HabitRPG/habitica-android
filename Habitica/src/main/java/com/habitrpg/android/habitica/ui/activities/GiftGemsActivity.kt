package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.navArgs
import com.habitrpg.android.habitica.HabiticaPurchaseVerifier
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class GiftGemsActivity : BaseActivity() {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private var purchaseHandler: PurchaseHandler? = null

    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private val avatarView: AvatarView by bindView(R.id.avatar_view)
    private val displayNameTextView: UsernameLabel by bindView(R.id.display_name_textview)
    private val usernameTextView: TextView by bindView(R.id.username_textview)

    private val gems4View: GemPurchaseOptionsView? by bindView(R.id.gems_4_view)
    private val gems21View: GemPurchaseOptionsView? by bindView(R.id.gems_21_view)
    private val gems42View: GemPurchaseOptionsView? by bindView(R.id.gems_42_view)
    private val gems84View: GemPurchaseOptionsView? by bindView(R.id.gems_84_view)

    private var giftedUsername: String? = null
    private var giftedUserID: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift_gems
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.gift_gems)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        giftedUserID = intent.getStringExtra("userID") ?: navArgs<GiftGemsActivityArgs>().value.userID
        giftedUsername = intent.getStringExtra("username") ?: navArgs<GiftGemsActivityArgs>().value.username

        compositeSubscription.add(socialRepository.getMember(giftedUsername ?: giftedUserID).subscribe(Consumer {
            avatarView.setAvatar(it)
            displayNameTextView.username = it.profile?.name
            displayNameTextView.tier = it.contributor?.level ?: 0
            usernameTextView.text = "@${it.username}"
            giftedUserID = it.id
            giftedUsername = it.username
        }, RxErrorHandler.handleEmptyError()))

        purchaseHandler?.getAllGemSKUs { skus ->
            for (sku in skus) {
                updateButtonLabel(sku.id.code, sku.price)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        purchaseHandler = PurchaseHandler(this, crashlyticsProxy)
        purchaseHandler?.startListening()
    }

    override fun onResume() {
        super.onResume()
        gems4View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase4Gems) })
        gems21View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase21Gems) })
        gems42View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase42Gems) })
        gems84View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase84Gems) })
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

    private fun updateButtonLabel(sku: String, price: String) {
        val matchingView: GemPurchaseOptionsView? = when (sku) {
            PurchaseTypes.Purchase4Gems -> gems4View
            PurchaseTypes.Purchase21Gems -> gems21View
            PurchaseTypes.Purchase42Gems -> gems42View
            PurchaseTypes.Purchase84Gems -> gems84View
            else -> return
        }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(price)
            matchingView.sku = sku
        }
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        purchaseHandler?.consumePurchase(event.purchase)
        runOnUiThread {
            displayConfirmationDialog()
        }
    }

    private fun displayConfirmationDialog() {
        val message = getString(R.string.gift_confirmation_text_gems, giftedUsername, "1")
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.gift_confirmation_title)
        alert.setMessage(message)
        alert.addOkButton { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        alert.enqueue()
    }


    fun purchaseGems(identifier: String) {
        HabiticaPurchaseVerifier.pendingGifts[identifier] = giftedUserID
        purchaseHandler?.purchaseGems(identifier)
    }
}