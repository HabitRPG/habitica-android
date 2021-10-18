package com.habitrpg.android.habitica

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent
import com.habitrpg.android.habitica.events.UserSubscribedEvent
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.solovyev.android.checkout.BasePurchaseVerifier
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.RequestListener
import org.solovyev.android.checkout.ResponseCodes
import retrofit2.HttpException
import java.util.*
import kotlin.math.abs

class HabiticaPurchaseVerifier(context: Context, apiClient: ApiClient) : BasePurchaseVerifier() {
    private val apiClient: ApiClient
    private val purchasedOrderList: MutableSet<String> = HashSet()
    private val context: Context
    override fun doVerify(purchases: List<Purchase>, requestListener: RequestListener<List<Purchase>>) {
        val verifiedPurchases: MutableList<Purchase> = ArrayList(purchases.size)
        val allPurchases = purchases.toMutableList()
        for (purchase in purchases) {
            if (purchasedOrderList.contains(purchase.orderId)) {
                verifiedPurchases.add(purchase)
                processedPurchase(purchase, allPurchases, verifiedPurchases, requestListener)
            } else {
                when {
                    PurchaseTypes.allGemTypes.contains(purchase.sku) -> {
                        val validationRequest = buildValidationRequest(purchase)
                        apiClient.validatePurchase(validationRequest).subscribe({
                            purchasedOrderList.add(purchase.orderId)
                            verifiedPurchases.add(purchase)
                            processedPurchase(purchase, allPurchases, verifiedPurchases, requestListener)
                            val gift = removeGift(purchase.sku)
                            EventBus.getDefault().post(ConsumablePurchasedEvent(purchase, gift?.second))
                        }) { throwable: Throwable ->
                            handleError(throwable, purchase, allPurchases, requestListener, verifiedPurchases)
                        }
                    }
                    PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku) -> {
                        val validationRequest = buildValidationRequest(purchase)
                        apiClient.validateNoRenewSubscription(validationRequest).subscribe({
                            purchasedOrderList.add(purchase.orderId)
                            verifiedPurchases.add(purchase)
                            processedPurchase(purchase, allPurchases, verifiedPurchases, requestListener)
                            val gift = removeGift(purchase.sku)
                            EventBus.getDefault().post(ConsumablePurchasedEvent(purchase, gift?.second))
                        }) { throwable: Throwable ->
                            handleError(throwable, purchase, allPurchases, requestListener, verifiedPurchases)
                        }
                    }
                    PurchaseTypes.allSubscriptionTypes.contains(purchase.sku) -> {
                        val validationRequest = SubscriptionValidationRequest()
                        validationRequest.sku = purchase.sku
                        validationRequest.transaction = Transaction()
                        validationRequest.transaction?.receipt = purchase.data
                        validationRequest.transaction?.signature = purchase.signature
                        apiClient.validateSubscription(validationRequest).subscribe({
                            purchasedOrderList.add(purchase.orderId)
                            verifiedPurchases.add(purchase)
                            processedPurchase(purchase, allPurchases, verifiedPurchases, requestListener)
                            FirebaseAnalytics.getInstance(context).logEvent("user_subscribed", null)
                            EventBus.getDefault().post(UserSubscribedEvent())
                        }) { throwable: Throwable ->
                            handleError(throwable, purchase, allPurchases, requestListener, verifiedPurchases)
                        }
                    }
                }
            }
        }
        preferences?.edit {
            putStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList)
        }
    }

    private fun processedPurchase(
        purchase: Purchase,
        allPurchases: MutableList<Purchase>,
        verifiedPurchases: MutableList<Purchase>,
        requestListener: RequestListener<List<Purchase>>
    ) {
        allPurchases.remove(purchase)
        if (allPurchases.isEmpty()) {
            if (verifiedPurchases.isEmpty()) {
                requestListener.onError(ResponseCodes.ERROR, Exception())
            } else {
                requestListener.onSuccess(verifiedPurchases)
            }
        }
    }

    private fun buildValidationRequest(purchase: Purchase): PurchaseValidationRequest {
        val validationRequest = PurchaseValidationRequest()
        validationRequest.sku = purchase.sku
        validationRequest.transaction = Transaction()
        validationRequest.transaction?.receipt = purchase.data
        validationRequest.transaction?.signature = purchase.signature
        pendingGifts[purchase.sku]?.let { gift ->
            // If the gift and the purchase happened within 5 minutes, we consider them to match.
            // Otherwise the gift is probably an old one that wasn't cleared out correctly
            if (abs(gift.first.time - purchase.time) < 300000) {
                validationRequest.gift = IAPGift(gift.second)
            } else {
                removeGift(purchase.sku)
            }
        }
        return validationRequest
    }

    private fun handleError(throwable: Throwable, purchase: Purchase,
                            allPurchases: MutableList<Purchase>,
                            requestListener: RequestListener<List<Purchase>>,
                            verifiedPurchases: MutableList<Purchase>) {
        (throwable as? HttpException)?.let { error ->
            if (error.code() == 401) {
                val res = apiClient.getErrorResponse(throwable)
                if (res.message != null && res.message == "RECEIPT_ALREADY_USED") {
                    purchasedOrderList.add(purchase.orderId)
                    verifiedPurchases.add(purchase)
                    processedPurchase(purchase, allPurchases, verifiedPurchases, requestListener)
                    EventBus.getDefault().post(ConsumablePurchasedEvent(purchase))
                    removeGift(purchase.sku)
                    return
                }
            }
        }
        FirebaseCrashlytics.getInstance().recordException(throwable)
        processedPurchase(purchase, allPurchases, verifiedPurchases, requestListener)
    }

    private fun loadPendingGifts(): MutableMap<String, Pair<Date, String>> {
        val outputMap: MutableMap<String, Pair<Date, String>> = HashMap()
        try {
            val jsonString = preferences?.getString(PENDING_GIFTS_KEY, JSONObject().toString()) ?: ""
            val jsonObject = JSONObject(jsonString)
            val keysItr = jsonObject.keys()
            while (keysItr.hasNext()) {
                val key = keysItr.next()
                val value = jsonObject.getJSONArray(key)
                outputMap[key] = Pair(value[0] as Date, value[1] as String)
            }
        } catch (e: Exception) {
            RxErrorHandler.reportError(e)
        }
        return outputMap
    }

    companion object {
        private const val PURCHASED_PRODUCTS_KEY = "PURCHASED_PRODUCTS"
        private const val PENDING_GIFTS_KEY = "PENDING_GIFTS_DATED"
        private var pendingGifts: MutableMap<String, Pair<Date, String>> = HashMap()
        private var preferences: SharedPreferences? = null

        fun addGift(sku: String, userID: String) {
            pendingGifts[sku] = Pair(Date(), userID)
            savePendingGifts()
        }

        private fun removeGift(sku: String): Pair<Date, String>? {
            val gift = pendingGifts.remove(sku)
            savePendingGifts()
            return gift
        }

        private fun savePendingGifts() {
            val jsonObject = JSONObject(pendingGifts as Map<*, *>)
            val jsonString = jsonObject.toString()
            preferences?.edit {
                putString(PENDING_GIFTS_KEY, jsonString)
            }
        }
    }

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.context = context
        preferences?.getStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList)
        pendingGifts = loadPendingGifts()
        this.apiClient = apiClient
    }
}
