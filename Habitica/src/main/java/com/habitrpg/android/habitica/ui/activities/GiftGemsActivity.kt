package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.navArgs
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityGiftGemsBinding
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.fragments.purchases.GiftBalanceGemsFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.GiftPurchaseGemsFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class GiftGemsActivity : BaseActivity() {

    private lateinit var binding: ActivityGiftGemsBinding

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private var purchaseHandler: PurchaseHandler? = null


    private var giftedUsername: String? = null
    private var giftedUserID: String? = null

    private var purchaseFragment: GiftPurchaseGemsFragment? = null
    private var balanceFragment: GiftBalanceGemsFragment? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift_gems
    }

    override fun getContentView(): View {
        binding = ActivityGiftGemsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.gift_gems)
        setSupportActionBar(binding.toolbar)

        purchaseHandler = PurchaseHandler(this, crashlyticsProxy)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        giftedUserID = intent.getStringExtra("userID")
        giftedUsername = intent.getStringExtra("username")
        if (giftedUserID == null && giftedUsername == null) {
            giftedUserID = navArgs<GiftGemsActivityArgs>().value.userID
            giftedUsername = navArgs<GiftGemsActivityArgs>().value.username
        }

        setViewPagerAdapter()

        compositeSubscription.add(socialRepository.getMember(giftedUsername ?: giftedUserID).firstElement().subscribe({
            giftedUserID = it.id
            giftedUsername = it.username
            purchaseFragment?.giftedMember = it
            balanceFragment?.giftedMember = it
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun onStart() {
        super.onStart()
        purchaseHandler?.startListening()
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

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager

        binding.viewPager.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {
                return if (position == 0) {
                     val fragment = GiftPurchaseGemsFragment()
                    fragment.setPurchaseHandler(purchaseHandler)
                    fragment.setupCheckout()
                    purchaseFragment = fragment
                    fragment
                } else {
                     val fragment = GiftBalanceGemsFragment()
                    fragment.onCompleted = {
                        displayConfirmationDialog()
                    }
                    balanceFragment = fragment
                    fragment
                }
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> getString(R.string.purchase)
                    1 -> getString(R.string.from_balance)
                    else -> ""
                }
            }
        }

        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        purchaseHandler?.consumePurchase(event.purchase)
        runOnUiThread {
            displayConfirmationDialog()
        }
    }

    private fun displayConfirmationDialog() {
        val message = getString(R.string.gift_confirmation_text_gems, giftedUsername)
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