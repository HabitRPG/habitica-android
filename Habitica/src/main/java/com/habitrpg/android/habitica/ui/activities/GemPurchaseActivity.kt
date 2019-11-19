package com.habitrpg.android.habitica.ui.activities


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.fragments.purchases.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionFragment
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class GemPurchaseActivity : BaseActivity() {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var userRepository: UserRepository

    internal var fragment: CheckoutFragment? = null
    var isActive = false
    var purchaseHandler: PurchaseHandler? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_gem_purchase
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        purchaseHandler?.onResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""

        purchaseHandler = PurchaseHandler(this, crashlyticsProxy)

        if (intent.extras?.containsKey("openSubscription") == true) {
            if (intent.extras?.getBoolean("openSubscription") == false) {
                createFragment(false)
            } else {
                createFragment(true)
            }
        } else {
            createFragment(true)
        }

        purchaseHandler?.whenCheckoutReady = {
            fragment?.setupCheckout()
        }
    }

    override fun onStart() {
        super.onStart()
        purchaseHandler?.startListening()
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
        purchaseHandler?.stopListening()
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun createFragment(showSubscription: Boolean) {
        val fragment: CheckoutFragment = if (showSubscription) {
            SubscriptionFragment()
        } else {
            GemsPurchaseFragment()
        }
        fragment.setPurchaseHandler(purchaseHandler)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment as Fragment)
                .commit()
        this.fragment = fragment
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        if (isActive) {
            purchaseHandler?.consumePurchase(event.purchase)
            compositeSubscription.add(userRepository.retrieveUser(false).subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
        }
    }

    interface CheckoutFragment {
        fun setupCheckout()
        fun setPurchaseHandler(handler: PurchaseHandler?)
    }
}
