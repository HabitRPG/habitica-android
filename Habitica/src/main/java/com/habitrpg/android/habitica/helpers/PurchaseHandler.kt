package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Intent
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import org.solovyev.android.checkout.*
import java.util.*

class PurchaseHandler(activity: Activity, val crashlyticsProxy: CrashlyticsProxy) {
    private val billing = HabiticaBaseApplication.getInstance(activity.applicationContext)?.billing
    private val checkout = billing?.let { Checkout.forActivity(activity, it) }
    private val inventory = checkout?.makeInventory()

    private var billingRequests: BillingRequests? = null
    private var isStarted = false

    var whenCheckoutReady: (() -> Unit)? = null

    init {
        handlers[activity] = this
    }

    fun startListening() {
        if (!isStarted) {
            checkout?.start()
            isStarted = true

            checkout?.whenReady(object : Checkout.Listener {
                override fun onReady(requests: BillingRequests) {
                    this@PurchaseHandler.billingRequests = requests
                    checkIfPendingPurchases()
                    whenCheckoutReady?.invoke()
                }

                override fun onReady(requests: BillingRequests, product: String, billingSupported: Boolean) {}
            })
        }
    }

    fun stopListening() {
        if (isStarted) {
            checkout?.stop()
            isStarted = false
        }
    }

    fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checkout?.onActivityResult(requestCode, resultCode, data)
    }

    fun getAllGemSKUs(onSuccess: ((List<Sku>) -> Unit)) {
        getSKUs(ProductTypes.IN_APP, PurchaseTypes.allGemTypes, onSuccess)
    }

    fun getAllSubscriptionSKUs(onSuccess: ((List<Sku>) -> Unit)) {
        getSKUs(ProductTypes.SUBSCRIPTION, PurchaseTypes.allSubscriptionTypes, onSuccess)
    }

    fun getAllSubscriptionProducts(onSuccess: ((Inventory.Product) -> Unit)) {
        getProduct(ProductTypes.SUBSCRIPTION, PurchaseTypes.allSubscriptionTypes, onSuccess)
    }

    fun getAllGiftSubscriptionProducts(onSuccess: ((Inventory.Product) -> Unit)) {
        getProduct(ProductTypes.IN_APP, PurchaseTypes.allSubscriptionNoRenewTypes, onSuccess)
    }

    fun getInAppPurchaseSKU(identifier: String, onSuccess: ((Sku) -> Unit)) {
        getSKU(ProductTypes.IN_APP, identifier, onSuccess)
    }

    fun getSubscriptionSKU(identifier: String, onSuccess: ((Sku) -> Unit)) {
        getSKU(ProductTypes.SUBSCRIPTION, identifier, onSuccess)
    }


    private fun getSKUs(type: String, identifiers: List<String>, onSuccess: ((List<Sku>) -> Unit)) {
        getProduct(type, identifiers) {
            onSuccess(it.skus)
        }
    }

    private fun getProduct(type: String, identifiers: List<String>, onSuccess: ((Inventory.Product) -> Unit)) {
        inventory?.load(Inventory.Request.create()
                .loadAllPurchases().loadSkus(type, identifiers)) { products ->
            val purchases = products.get(type)
            if (!purchases.supported) return@load
            onSuccess(purchases)
        }
    }

    private fun getSKU(type: String, identifier: String, onSuccess: ((Sku) -> Unit)) {
        inventory?.load(Inventory.Request.create()
                .loadAllPurchases().loadSkus(type, listOf(identifier))) { products ->
            val purchases = products.get(type)
            if (!purchases.supported) return@load
            purchases.skus.firstOrNull()?.let { onSuccess(it) }
        }
    }

    fun purchaseSubscription(sku: Sku) {
        sku.id.code?.let { code ->
            billingRequests?.isPurchased(ProductTypes.SUBSCRIPTION, code, object : RequestListener<Boolean> {
                override fun onSuccess(aBoolean: Boolean) {
                    if (!aBoolean) {
                        // no current product exist
                        checkout?.let {
                            billingRequests?.purchase(ProductTypes.SUBSCRIPTION, code, null, it.purchaseFlow)
                        }
                    }
                }

                override fun onError(i: Int, e: Exception) {}
            })
        }
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

    fun purchaseGems(identifier: String) {
        checkout?.let {
            it.destroyPurchaseFlow()
            billingRequests?.purchase(ProductTypes.IN_APP, identifier, null, it.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                override fun onSuccess(result: Purchase) {
                    billingRequests?.consume(result.token, object : RequestListener<Any> {
                        override fun onSuccess(o: Any) {
                        }

                        override fun onError(i: Int, e: Exception) {
                            crashlyticsProxy.fabricLogE("PurchaseConsumeException", "Consume", e)
                        }
                    })
                }

                override fun onError(response: Int, e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }))
        }
    }

    fun purchaseNoRenewSubscription(sku: Sku) {
        checkout?.let {
            billingRequests?.purchase(ProductTypes.IN_APP, sku.id.code, null, it.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                override fun onSuccess(result: Purchase) {
                    billingRequests?.consume(result.token, object : RequestListener<Any> {
                        override fun onSuccess(o: Any) {
                        }

                        override fun onError(i: Int, e: Exception) {
                            crashlyticsProxy.fabricLogE("PurchaseConsumeException", "Consume", e)
                        }
                    })
                }

                override fun onError(response: Int, e: java.lang.Exception) {

                }
            }))
        }
    }

    fun consumePurchase(purchase: Purchase) {
        if (PurchaseTypes.allGemTypes.contains(purchase.sku) || PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
            billingRequests?.consume(purchase.token, object : RequestListener<Any> {
                override fun onSuccess(result: Any) {}

                override fun onError(response: Int, e: Exception) {
                    crashlyticsProxy.fabricLogE("PurchaseConsumeException", "Consume", e)
                }
            })
        }
    }
    
    companion object {

        private var handlers = WeakHashMap<Activity, PurchaseHandler>()

        fun findForActivity(activity: Activity): PurchaseHandler? {
            return handlers[activity]
        }
    }
}