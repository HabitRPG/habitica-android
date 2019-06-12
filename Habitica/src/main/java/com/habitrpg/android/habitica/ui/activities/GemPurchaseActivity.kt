package com.habitrpg.android.habitica.ui.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.ABTest
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.SubscriptionFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.playseeds.android.sdk.Seeds
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.*
import java.util.*
import javax.inject.Inject

class GemPurchaseActivity : BaseActivity(), InAppMessageListener {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository

    internal val tabLayout: TabLayout by bindView(R.id.tab_layout)
    internal val viewPager: androidx.viewpager.widget.ViewPager by bindView(R.id.viewPager)

    internal var fragments: MutableList<CheckoutFragment> = ArrayList()
    var isActive = false
    var activityCheckout: ActivityCheckout? = null
        private set
    private var billingRequests: BillingRequests? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_gem_purchase
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityCheckout?.onActivityResult(requestCode, resultCode, data)
    }

    private var showSubscriptionPageFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Seeds.sharedInstance()
                .simpleInit(this, this, "https://dash.playseeds.com", getString(R.string.seeds_app_key)).isLoggingEnabled = true
        Seeds.sharedInstance().requestInAppMessage(getString(R.string.seeds_interstitial_gems))
        Seeds.sharedInstance().requestInAppMessage(getString(R.string.seeds_interstitial_sharing))

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.gem_purchase_toolbartitle)

        viewPager.currentItem = 0

        setViewPagerAdapter()

        compositeSubscription.add(userRepository.getUser().subscribe(Consumer { user ->
            for (test in user.abTests ?: emptyList<ABTest>()) {
                if (test.name == "subscriptionPageOrder") {
                    if (test.group == "subscriptionFirst") {
                        showSubscriptionPageFirst = true
                        viewPager.adapter?.notifyDataSetChanged()
                        return@Consumer
                    }
                }
            }
            showSubscriptionPageFirst = false
            viewPager.adapter?.notifyDataSetChanged()
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun onStart() {
        super.onStart()
        setupCheckout()

        activityCheckout?.destroyPurchaseFlow()

        activityCheckout?.createPurchaseFlow(object : RequestListener<Purchase> {
            override fun onSuccess(purchase: Purchase) {

            }

            override fun onError(i: Int, e: Exception) {
                crashlyticsProxy.fabricLogE("PurchaseFlowException", "Error", e)
                val billingError = e as? BillingException
                if (billingError != null) {
                    Log.e("BILLING ERROR", billingError.toString())
                }
            }
        })


        activityCheckout?.whenReady(object : Checkout.Listener {
            override fun onReady(billingRequests: BillingRequests) {
                this@GemPurchaseActivity.billingRequests = billingRequests

                for (fragment in fragments) {
                    fragment.setBillingRequests(billingRequests)
                }

                checkIfPendingPurchases()
            }

            override fun onReady(billingRequests: BillingRequests, s: String, b: Boolean) {}
        })
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    public override fun onStop() {
        activityCheckout?.stop()
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupCheckout() {
        HabiticaBaseApplication.getInstance(this)?.billing?.let {
            activityCheckout = Checkout.forActivity(this, it)
            activityCheckout?.start()
        }

    }

    override fun inAppMessageClicked(messageId: String) {
        for (fragment in fragments) {
            if (fragment.javaClass.isAssignableFrom(GemsPurchaseFragment::class.java)) {
                (fragment as? GemsPurchaseFragment)?.purchaseGems(PurchaseTypes.Purchase84Gems)
            }
        }
    }

    override fun inAppMessageDismissed(messageId: String) {

    }

    override fun inAppMessageLoadSucceeded(messageId: String) {

    }

    override fun inAppMessageShown(messageId: String, succeeded: Boolean) {

    }

    override fun noInAppMessageFound(messageId: String) {

    }

    override fun inAppMessageClickedWithDynamicPrice(messageId: String, price: Double?) {

    }

    fun showSeedsPromo(messageId: String, context: String) {
        try {
            runOnUiThread {
                if (Seeds.sharedInstance().isInAppMessageLoaded(messageId)) {
                    Seeds.sharedInstance().showInAppMessage(messageId, context)
                } else {
                    // Skip the interstitial showing this time and try to reload the interstitial
                    Seeds.sharedInstance().requestInAppMessage(messageId)
                }
            }
        } catch (e: Exception) {
            println("Exception: $e")
        }

    }

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager

        viewPager.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {
                val gemPurchasePosition = if (showSubscriptionPageFirst) 1 else 0
                val fragment: CheckoutFragment = if (position == gemPurchasePosition) {
                    GemsPurchaseFragment()
                } else {
                    SubscriptionFragment()
                }
                if (fragments.size > position) {
                    fragments[position] = fragment
                } else {
                    fragments.add(fragment)
                }
                fragment.setListener(this@GemPurchaseActivity)
                fragment.setupCheckout()
                if (billingRequests != null) {
                    fragment.setBillingRequests(billingRequests)
                }
                return fragment as androidx.fragment.app.Fragment
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                val gemPurchasePosition = if (showSubscriptionPageFirst) 1 else 0
                return when (position) {
                    gemPurchasePosition -> getString(R.string.gems)
                    else -> getString(R.string.subscriptions)
                }
            }
        }

        tabLayout.setupWithViewPager(viewPager)
    }

    private fun checkIfPendingPurchases() {
        billingRequests?.getAllPurchases(ProductTypes.IN_APP, object : RequestListener<Purchases> {
            override fun onSuccess(purchases: Purchases) {
                for (purchase in purchases.list) {
                    if (PurchaseTypes.allGemTypes.contains(purchase.sku)) {
                        billingRequests?.consume(purchase.token, object : RequestListener<Any> {
                            override fun onSuccess(o: Any) {
                                //EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                            }

                            override fun onError(i: Int, e: Exception) {
                                crashlyticsProxy.fabricLogE("Purchase", "Consume", e)
                            }
                        })
                    }
                }
            }

            override fun onError(i: Int, e: Exception) {
                crashlyticsProxy.fabricLogE("Purchase", "getAllPurchases", e)
            }
        })
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        if (isActive) {
            consumePurchase(event.purchase)
        }
    }

    interface CheckoutFragment {

        fun setupCheckout()

        fun setListener(listener: GemPurchaseActivity)

        fun setBillingRequests(billingRequests: BillingRequests?)
    }

    private fun consumePurchase(purchase: Purchase) {
        if (PurchaseTypes.allGemTypes.contains(purchase.sku) || PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
            billingRequests?.consume(purchase.token, object : RequestListener<Any> {

                override fun onSuccess(result: Any) {

                }

                override fun onError(response: Int, e: Exception) {
                    crashlyticsProxy.fabricLogE("PurchaseConsumeException", "Consume", e)
                }
            })
        }
    }
}
