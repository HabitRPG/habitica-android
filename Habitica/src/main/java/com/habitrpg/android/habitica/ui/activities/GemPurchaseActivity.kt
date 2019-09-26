package com.habitrpg.android.habitica.ui.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.SubscriptionFragment
import org.greenrobot.eventbus.Subscribe
import org.solovyev.android.checkout.*
import javax.inject.Inject

class GemPurchaseActivity : BaseActivity() {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository

    internal var fragment: CheckoutFragment? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.gem_purchase_toolbartitle)

        if (intent.extras?.containsKey("openSubscription") == true) {
            if (intent.extras?.getBoolean("openSubscription") == false) {
                createFragment(false)
            } else {
                createFragment(true)
            }
        } else {
            createFragment(true)
        }
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

                fragment?.setBillingRequests(billingRequests)

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

    private fun createFragment(showSubscription: Boolean) {
        val fragment: CheckoutFragment = if (showSubscription) {
            SubscriptionFragment()
        } else {
            GemsPurchaseFragment()
        }
        fragment.setListener(this@GemPurchaseActivity)
        fragment.setupCheckout()
        if (billingRequests != null) {
            fragment.setBillingRequests(billingRequests)
        }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment as Fragment)
                .commit()
        this.fragment = fragment
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
