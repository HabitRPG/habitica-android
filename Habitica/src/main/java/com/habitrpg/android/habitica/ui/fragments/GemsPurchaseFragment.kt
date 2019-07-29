package com.habitrpg.android.habitica.ui.fragments

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import org.solovyev.android.checkout.BillingRequests
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.ProductTypes
import javax.inject.Inject

class GemsPurchaseFragment : BaseFragment(), GemPurchaseActivity.CheckoutFragment {

    private val gems4View: GemPurchaseOptionsView? by bindView(R.id.gems_4_view)
    private val gems21View: GemPurchaseOptionsView? by bindView(R.id.gems_21_view)
    private val gems42View: GemPurchaseOptionsView? by bindView(R.id.gems_42_view)
    private val gems84View: GemPurchaseOptionsView? by bindView(R.id.gems_84_view)
    private val supportTextView: TextView? by bindView(R.id.supportTextView)

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy

    private var listener: GemPurchaseActivity? = null
    private var billingRequests: BillingRequests? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        return container?.inflate(R.layout.fragment_gem_purchase)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gems4View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase4Gems) })
        gems21View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase21Gems) })
        gems42View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase42Gems) })
        gems84View?.setOnPurchaseClickListener(View.OnClickListener { purchaseGems(PurchaseTypes.Purchase84Gems) })

        val heartDrawable = BitmapDrawable(resources, HabiticaIconsHelper.imageOfHeartLarge())
        supportTextView?.setCompoundDrawables(null, heartDrawable, null, null)

        gems84View?.seedsImageButton?.setOnClickListener { (this.activity as? GemPurchaseActivity)?.showSeedsPromo(getString(R.string.seeds_interstitial_gems), "store") }
    }

    override fun setupCheckout() {
        val checkout = listener?.activityCheckout
        if (checkout != null) {
            val inventory = checkout.makeInventory()

            inventory.load(Inventory.Request.create()
                    .loadAllPurchases().loadSkus(ProductTypes.IN_APP, PurchaseTypes.allGemTypes)
            ) { products ->
                val gems = products.get(ProductTypes.IN_APP)
                if (!gems.supported) {
                    // billing is not supported, user can't purchase anything
                    return@load
                }
                val skus = gems.skus
                for (sku in skus) {
                    updateButtonLabel(sku.id.code, sku.price)
                }
            }
        }
    }

    override fun setListener(listener: GemPurchaseActivity) {
        this.listener = listener
    }

    override fun setBillingRequests(billingRequests: BillingRequests?) {
        this.billingRequests = billingRequests
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

    fun purchaseGems(sku: String) {
        listener?.activityCheckout?.let {
            billingRequests?.purchase(ProductTypes.IN_APP, sku, null, it.purchaseFlow)
        }
    }
}
