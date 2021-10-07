package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Intent
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.solovyev.android.checkout.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class PurchaseHandler(activity: Activity, val analyticsManager: AnalyticsManager) {
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
    suspend fun getAllGemSKUs(): List<Sku> = getSKUs(ProductTypes.IN_APP, PurchaseTypes.allGemTypes)
    suspend fun getAllSubscriptionProducts() = getProduct(ProductTypes.SUBSCRIPTION, PurchaseTypes.allSubscriptionTypes)
    suspend fun getAllGiftSubscriptionProducts() = getProduct(ProductTypes.IN_APP, PurchaseTypes.allSubscriptionNoRenewTypes)
    suspend fun getInAppPurchaseSKU(identifier: String) = getSKU(ProductTypes.IN_APP, identifier)

    private suspend fun getSKUs(type: String, identifiers: List<String>): List<Sku> {
        return getProduct(type, identifiers)?.skus ?: emptyList()
    }

    private suspend fun getProduct(type: String, identifiers: List<String>): Inventory.Product? {
        val inventory = loadInventory(type, identifiers)
        val purchases = inventory?.get(type) ?: return null
        if (!purchases.supported) return null
        return purchases
    }

    private suspend fun getSKU(type: String, identifier: String): Sku? {
        val inventory = loadInventory(type, listOf(identifier))
        val purchases = inventory?.get(type) ?: return null
        if (!purchases.supported) return null
        return purchases.skus.firstOrNull()
    }

    private suspend fun loadInventory(type: String, skus: List<String>): Inventory.Products? = withContext(Dispatchers.Main) {
        suspendCoroutine { cont ->
            val request = Inventory.Request.create().loadAllPurchases().loadSkus(type, skus)
            try {
                inventory?.load(request) {
                    cont.resume(it)
                }
            } catch (e: NullPointerException) {
                cont.resumeWithException(e)
            }
            if (inventory == null) cont.resume(null)
        }
    }

    fun purchaseSubscription(sku: Sku, onSuccess: (() -> Unit)) {
        sku.id.code?.let { code ->
            billingRequests?.isPurchased(
                ProductTypes.SUBSCRIPTION, code,
                object : RequestListener<Boolean> {
                    override fun onSuccess(aBoolean: Boolean) {
                        if (!aBoolean) {
                            // no current product exist
                            checkout?.let {
                                billingRequests?.purchase(
                                    ProductTypes.SUBSCRIPTION, code, null,
                                    it.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                                        override fun onSuccess(result: Purchase) {
                                            onSuccess()
                                        }

                                        override fun onError(response: Int, e: java.lang.Exception) { analyticsManager.logException(e) }
                                    })
                                )
                            }
                        } else {
                            onSuccess()
                        }
                    }

                    override fun onError(i: Int, e: Exception) { analyticsManager.logException(e) }
                }
            )
        }
    }

    fun checkForSubscription(onSubscriptionFound: ((Purchase) -> Unit)) {
        billingRequests?.getPurchases(
            ProductTypes.SUBSCRIPTION, null,
            object : RequestListener<Purchases> {
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
            }
        )
    }

    private fun checkIfPendingPurchases() {
        billingRequests?.getAllPurchases(
            ProductTypes.IN_APP,
            object : RequestListener<Purchases> {
                override fun onSuccess(purchases: Purchases) {
                    for (purchase in purchases.list) {
                        if (PurchaseTypes.allGemTypes.contains(purchase.sku)) {
                            billingRequests?.consume(
                                purchase.token,
                                object : RequestListener<Any> {
                                    override fun onSuccess(o: Any) {
                                        // EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                                    }

                                    override fun onError(i: Int, e: Exception) {
                                        analyticsManager.logException(e)
                                    }
                                }
                            )
                        }
                    }
                }

                override fun onError(i: Int, e: Exception) {
                    analyticsManager.logException(e)
                }
            }
        )
    }

    fun purchaseGems(identifier: String) {
        checkout?.let {
            it.destroyPurchaseFlow()
            billingRequests?.purchase(
                ProductTypes.IN_APP, identifier, null,
                it.createOneShotPurchaseFlow(
                    PURCHASE_REQUEST_CODE,
                    object : RequestListener<Purchase> {
                        override fun onSuccess(result: Purchase) {
                            billingRequests?.consume(
                                result.token,
                                object : RequestListener<Any> {
                                    override fun onSuccess(o: Any) { /* no-op */ }

                                    override fun onError(i: Int, e: Exception) {
                                        analyticsManager.logException(e)
                                    }
                                }
                            )
                        }

                        override fun onError(response: Int, e: java.lang.Exception) {
                            analyticsManager.logException(e)
                            if (response == ResponseCodes.ITEM_ALREADY_OWNED) {
                                checkIfPendingPurchases()
                            }
                        }
                    }
                )
            )
        }
    }

    fun purchaseNoRenewSubscription(sku: Sku) {
        checkout?.let {
            billingRequests?.purchase(
                ProductTypes.IN_APP, sku.id.code, null,
                it.createOneShotPurchaseFlow(
                    PURCHASE_REQUEST_CODE,
                    object : RequestListener<Purchase> {
                        override fun onSuccess(result: Purchase) {
                            billingRequests?.consume(
                                result.token,
                                object : RequestListener<Any> {
                                    override fun onSuccess(o: Any) { /* no-op */ }

                                    override fun onError(i: Int, e: Exception) {
                                        analyticsManager.logException(e)
                                    }
                                }
                            )
                        }

                        override fun onError(response: Int, e: java.lang.Exception) {
                            analyticsManager.logException(e)
                        }
                    }
                )
            )
        }
    }

    fun consumePurchase(purchase: Purchase) {
        if (PurchaseTypes.allGemTypes.contains(purchase.sku) || PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
            billingRequests?.consume(
                purchase.token,
                object : RequestListener<Any> {
                    override fun onSuccess(result: Any) { /* no-op */ }

                    override fun onError(response: Int, e: Exception) {
                        analyticsManager.logException(e)
                    }
                }
            )
        }
    }

    companion object {

        private var handlers = WeakHashMap<Activity, PurchaseHandler>()

        val PURCHASE_REQUEST_CODE = 51966

        fun findForActivity(activity: Activity): PurchaseHandler? {
            return handlers[activity]
        }
    }
}
