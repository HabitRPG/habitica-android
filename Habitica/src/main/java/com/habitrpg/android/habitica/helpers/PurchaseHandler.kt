package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Intent
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import org.solovyev.android.checkout.*
import java.util.*

class PurchaseHandler(activity: Activity, val analyticsManager: AnalyticsManager) {
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

                override fun onReady(requests: BillingRequests, product: String, billingSupported: Boolean) { /* no-op */ }
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

    fun getAllSubscriptionProducts(onSuccess: ((Inventory.Product) -> Unit)) {
        getProduct(ProductTypes.SUBSCRIPTION, PurchaseTypes.allSubscriptionTypes, onSuccess)
    }

    fun getAllGiftSubscriptionProducts(onSuccess: ((Inventory.Product) -> Unit)) {
        getProduct(ProductTypes.IN_APP, PurchaseTypes.allSubscriptionNoRenewTypes, onSuccess)
    }

    fun getInAppPurchaseSKU(identifier: String, onSuccess: ((Sku) -> Unit)) {
        getSKU(ProductTypes.IN_APP, identifier, onSuccess)
    }


    private fun getSKUs(type: String, identifiers: List<String>, onSuccess: ((List<Sku>) -> Unit)) {
        getProduct(type, identifiers) {
            onSuccess(it.skus)
        }
    }

    private fun getProduct(type: String, identifiers: List<String>, onSuccess: ((Inventory.Product) -> Unit)) {
        loadInventory(type, identifiers, Inventory.Callback { products ->
            val purchases = products.get(type)
            if (!purchases.supported) return@Callback
            onSuccess(purchases)
        })
    }

    private fun getSKU(type: String, identifier: String, onSuccess: ((Sku) -> Unit)) {
        loadInventory(type, listOf(identifier), Inventory.Callback { products ->
            val purchases = products.get(type)
            if (!purchases.supported) return@Callback
            purchases.skus.firstOrNull()?.let { onSuccess(it) }
        })
    }

    private fun loadInventory(type: String, skus: List<String>, callback: Inventory.Callback) {
        val request = Inventory.Request.create().loadAllPurchases().loadSkus(type, skus)
        if (request != null) {
            try {
                inventory?.load(request, callback)
            } catch (e: NullPointerException) {
                return
            }
        }
    }

    fun purchaseSubscription(sku: Sku, onSuccess: (() -> Unit)) {
        sku.id.code?.let { code ->
            billingRequests?.isPurchased(ProductTypes.SUBSCRIPTION, code, object : RequestListener<Boolean> {
                override fun onSuccess(aBoolean: Boolean) {
                    if (!aBoolean) {
                        // no current product exist
                        checkout?.let {
                            billingRequests?.purchase(ProductTypes.SUBSCRIPTION, code, null, it.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                                override fun onSuccess(result: Purchase) {
                                    onSuccess()
                                }

                                override fun onError(response: Int, e: java.lang.Exception) {}
                            }))
                        }
                    } else {
                        onSuccess()
                    }
                }

                override fun onError(i: Int, e: Exception) { analyticsManager.logException(e) }
            })
        }
    }

    fun checkForSubscription(onSubscriptionFound: ((Purchase) -> Unit)) {
        billingRequests?.getPurchases(ProductTypes.SUBSCRIPTION, null, object : RequestListener<Purchases> {
            override fun onSuccess(result: Purchases) {
                var lastPurchase: Purchase? = null
                for (purchase in result.list) {
                    if (lastPurchase != null && lastPurchase.time > purchase.time) {
                        continue
                    } else {
                        lastPurchase = purchase
                    }
                }
                if (lastPurchase != null) {
                    onSubscriptionFound(lastPurchase)
                }
            }

            override fun onError(response: Int, e: java.lang.Exception) {
            }
        })
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
                                analyticsManager.logException(e)
                            }
                        })
                    }
                }
            }

            override fun onError(i: Int, e: Exception) {
                analyticsManager.logException(e)
            }
        })
    }

    fun purchaseGems(identifier: String) {
        checkout?.let {
            it.destroyPurchaseFlow()
            billingRequests?.purchase(ProductTypes.IN_APP, identifier, null, it.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                override fun onSuccess(result: Purchase) {
                    billingRequests?.consume(result.token, object : RequestListener<Any> {
                        override fun onSuccess(o: Any) { /* no-op */ }

                        override fun onError(i: Int, e: Exception) {
                            analyticsManager.logException(e)
                        }
                    })
                }

                override fun onError(response: Int, e: java.lang.Exception) {
                    analyticsManager.logException(e)
                    if (response == ResponseCodes.ITEM_ALREADY_OWNED) {
                        checkIfPendingPurchases()
                    }
                }
            }))
        }
    }

    fun purchaseNoRenewSubscription(sku: Sku) {
        checkout?.let {
            billingRequests?.purchase(ProductTypes.IN_APP, sku.id.code, null, it.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                override fun onSuccess(result: Purchase) {
                    billingRequests?.consume(result.token, object : RequestListener<Any> {
                        override fun onSuccess(o: Any) { /* no-op */ }

                        override fun onError(i: Int, e: Exception) {
                            analyticsManager.logException(e)
                        }
                    })
                }

                override fun onError(response: Int, e: java.lang.Exception) {
                    analyticsManager.logException(e)
                }
            }))
        }
    }

    fun consumePurchase(purchase: Purchase) {
        if (PurchaseTypes.allGemTypes.contains(purchase.sku) || PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
            billingRequests?.consume(purchase.token, object : RequestListener<Any> {
                override fun onSuccess(result: Any) { /* no-op */ }

                override fun onError(response: Int, e: Exception) {
                    analyticsManager.logException(e)
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