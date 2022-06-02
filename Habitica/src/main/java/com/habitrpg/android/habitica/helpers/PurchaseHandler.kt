package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.querySkuDetails
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.common.habitica.models.IAPGift
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import com.habitrpg.common.habitica.models.Transaction
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.activities.PurchaseActivity
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.core.Flowable
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class PurchaseHandler(
    private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val apiClient: ApiClient,
    private val userViewModel: MainUserViewModel
) :
    PurchasesUpdatedListener, PurchasesResponseListener {
    private val billingClient = BillingClient
        .newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.let { processPurchases(result, it) }
    }

    override fun onQueryPurchasesResponse(result: BillingResult, purchases: MutableList<Purchase>) {
        processPurchases(result, purchases)
    }

    private fun processPurchases(result: BillingResult, purchases: MutableList<Purchase>) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                for (purchase in purchases) {
                    handle(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                return
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    for (purchase in purchases) {
                        consume(purchase)
                    }
                }
            }
            else -> {
                FirebaseCrashlytics.getInstance().recordException(Throwable(result.debugMessage))
            }
        }
    }

    init {
        startListening()
    }

    private var billingClientState: BillingClientState = BillingClientState.UNINITIALIZED

    private enum class BillingClientState {
        UNINITIALIZED,
        READY,
        UNAVAILABLE,
        DISCONNECTED;

        val canMaybePurchase: Boolean
            get() {
                return this == UNINITIALIZED || this == READY
            }
    }

    fun startListening() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClientState = BillingClientState.READY
                    queryPurchases()
                } else {
                    billingClientState = BillingClientState.UNAVAILABLE
                }
            }

            override fun onBillingServiceDisconnected() {
                billingClientState = BillingClientState.DISCONNECTED
            }
        })
    }

    fun stopListening() {
        billingClient.endConnection()
    }

    fun queryPurchases(){
        if (billingClientState == BillingClientState.READY){
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.SUBS,
                this@PurchaseHandler
            )
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.INAPP,
                this@PurchaseHandler
            )
        }
    }

    suspend fun getAllGemSKUs(): List<SkuDetails> =
        getSKUs(BillingClient.SkuType.INAPP, PurchaseTypes.allGemTypes)

    suspend fun getAllSubscriptionProducts() =
        getSKUs(BillingClient.SkuType.SUBS, PurchaseTypes.allSubscriptionTypes)

    suspend fun getAllGiftSubscriptionProducts() =
        getSKUs(BillingClient.SkuType.INAPP, PurchaseTypes.allSubscriptionNoRenewTypes)

    suspend fun getInAppPurchaseSKU(identifier: String) =
        getSKU(BillingClient.SkuType.INAPP, identifier)

    private suspend fun getSKUs(type: String, identifiers: List<String>): List<SkuDetails> {
        return loadInventory(type, identifiers) ?: emptyList()
    }

    private suspend fun getSKU(type: String, identifier: String): SkuDetails? {
        val inventory = loadInventory(type, listOf(identifier))
        return inventory?.firstOrNull()
    }

    private suspend fun loadInventory(type: String, skus: List<String>): List<SkuDetails>? {
        retryUntil { billingClientState.canMaybePurchase && billingClient.isReady }
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skus)
            .setType(type)
            .build()
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params)
        }
        return skuDetailsResult.skuDetailsList
    }

    fun purchase(activity: Activity, skuDetails: SkuDetails, recipient: String? = null) {
        recipient?.let {
            addGift(skuDetails.sku, it)
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    private suspend fun consume(purchase: Purchase, retries: Int = 4) {
        retryUntil { billingClientState.canMaybePurchase && billingClient.isReady }
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val result = billingClient.consumePurchase(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            delay(500)
            consume(purchase, retries - 1)
        }
    }

    private fun handle(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
        }
        val sku = purchase.skus.firstOrNull()
        when {
            PurchaseTypes.allGemTypes.contains(sku) -> {
                val validationRequest = buildValidationRequest(purchase)
                apiClient.validatePurchase(validationRequest).subscribe({
                    processedPurchase(purchase)
                    val gift = removeGift(sku)
                    CoroutineScope(Dispatchers.IO).launch {
                        consume(purchase)
                    }
                    displayConfirmationDialog(purchase, gift?.second)
                }) { throwable: Throwable ->
                    handleError(throwable, purchase)
                }
            }
            PurchaseTypes.allSubscriptionNoRenewTypes.contains(sku) -> {
                val validationRequest = buildValidationRequest(purchase)
                apiClient.validateNoRenewSubscription(validationRequest).subscribe({
                    processedPurchase(purchase)
                    val gift = removeGift(sku)
                    CoroutineScope(Dispatchers.IO).launch {
                        consume(purchase)
                    }
                    displayConfirmationDialog(purchase, gift?.second)
                }) { throwable: Throwable ->
                    handleError(throwable, purchase)
                }
            }
            PurchaseTypes.allSubscriptionTypes.contains(sku) -> {
                val plan = userViewModel.user.value?.purchased?.plan
                if (plan?.isActive == true) {
                    if (plan.additionalData?.data?.orderId == purchase.orderId &&
                        ((plan.dateTerminated != null) == purchase.isAutoRenewing)
                    ) {
                        return
                    }
                }
                val validationRequest = buildValidationRequest(purchase)
                apiClient.validateSubscription(validationRequest).subscribe({
                    processedPurchase(purchase)
                    analyticsManager.logEvent("user_subscribed", bundleOf(Pair("sku", sku)))
                    CoroutineScope(Dispatchers.IO).launch {
                        acknowledgePurchase(purchase)
                    }
                    displayConfirmationDialog(purchase)
                }) { throwable: Throwable ->
                    handleError(throwable, purchase)
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase, retries: Int = 4) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val response = billingClient.acknowledgePurchase(params)
        if (response.responseCode != BillingClient.BillingResponseCode.OK) {
            delay(500)
            acknowledgePurchase(purchase, retries - 1)
        }
    }

    private fun processedPurchase(purchase: Purchase) {
        userViewModel.userRepository.retrieveUser(false, true).subscribeWithErrorHandler {}
    }

    private fun buildValidationRequest(purchase: Purchase): PurchaseValidationRequest {
        val validationRequest = PurchaseValidationRequest()
        validationRequest.sku = purchase.skus.firstOrNull()
        validationRequest.transaction = Transaction()
        validationRequest.transaction?.receipt = purchase.originalJson
        validationRequest.transaction?.signature = purchase.signature
        pendingGifts[validationRequest.sku]?.let { gift ->
            // If the gift and the purchase happened within 5 minutes, we consider them to match.
            // Otherwise the gift is probably an old one that wasn't cleared out correctly
            if (kotlin.math.abs(gift.first.time - purchase.purchaseTime) < 5.toDuration(DurationUnit.MINUTES).inWholeMilliseconds) {
                validationRequest.gift = IAPGift(gift.second)
            } else {
                removeGift(validationRequest.sku ?: "")
            }
        }
        return validationRequest
    }

    private fun handleError(throwable: Throwable, purchase: Purchase) {
        (throwable as? HttpException)?.let { error ->
            if (error.code() == 401) {
                val res = apiClient.getErrorResponse(throwable)
                if (res.message != null && res.message == "RECEIPT_ALREADY_USED") {
                    processedPurchase(purchase)
                    removeGift(purchase.skus.firstOrNull())
                    CoroutineScope(Dispatchers.IO).launch {
                        consume(purchase)
                    }
                    return
                }
            }
        }
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    suspend fun checkForSubscription(): Purchase? {
        val result = withContext(Dispatchers.IO) {
            billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        }
        var fallback: Purchase? = null
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val purchases = result.purchasesList.filter { it.isAcknowledged }.sortedByDescending { it.purchaseTime }
            // If there is a subscription that is still active, prioritise that. Otherwise return the most recent one.
            for (purchase in purchases) {
                if (purchase.isAutoRenewing) {
                    return purchase
                } else if (!purchase.isAutoRenewing && fallback == null) {
                    fallback = purchase
                }
            }
        }
        return fallback
    }

    fun cancelSubscription(): Flowable<User> {
        return apiClient.cancelSubscription()
            .flatMap { userViewModel.userRepository.retrieveUser(false, true) }
    }

    private fun durationString(sku: String): String {
        return when (sku) {
            PurchaseTypes.Subscription1MonthNoRenew, PurchaseTypes.Subscription1Month -> "1"
            PurchaseTypes.Subscription3MonthNoRenew, PurchaseTypes.Subscription3Month -> "3"
            PurchaseTypes.Subscription6MonthNoRenew, PurchaseTypes.Subscription6Month -> "6"
            PurchaseTypes.Subscription12MonthNoRenew, PurchaseTypes.Subscription12Month -> "12"
            else -> ""
        }
    }

    private fun gemAmountString(sku: String): String {
        return when (sku) {
            PurchaseTypes.Purchase4Gems -> "4"
            PurchaseTypes.Purchase21Gems -> "21"
            PurchaseTypes.Purchase42Gems -> "42"
            PurchaseTypes.Purchase84Gems -> "84"
            else -> ""
        }
    }

    private fun displayConfirmationDialog(purchase: Purchase, giftedTo: String? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            val application = (context as? HabiticaBaseApplication)
                ?: (context.applicationContext as? HabiticaBaseApplication) ?: return@launch
            val sku = purchase.skus.firstOrNull() ?: return@launch
            var title = context.getString(R.string.successful_purchase_generic)
            val message = when {
                PurchaseTypes.allSubscriptionNoRenewTypes.contains(sku) -> {
                    title = context.getString(R.string.gift_confirmation_title)
                    context.getString(R.string.gift_confirmation_text_sub, giftedTo, durationString(sku))
                }
                PurchaseTypes.allSubscriptionTypes.contains(sku) -> {
                    if (sku == PurchaseTypes.Subscription1Month) {
                        context.getString(R.string.subscription_confirmation)
                    } else {
                        context.getString(R.string.subscription_confirmation_multiple, durationString(sku))
                    }
                }
                PurchaseTypes.allGemTypes.contains(sku) && giftedTo != null -> {
                    title = context.getString(R.string.gift_confirmation_title)
                    context.getString(R.string.gift_confirmation_text_gems_new, giftedTo, gemAmountString(sku))
                }
                PurchaseTypes.allGemTypes.contains(sku) && giftedTo == null -> {
                    context.getString(R.string.gem_purchase_confirmation, gemAmountString(sku))
                }
                else -> null
            }
            application.currentActivity?.get()?.let { activity ->
                val alert = HabiticaAlertDialog(activity)
                alert.setTitle(title)
                message?.let { alert.setMessage(it) }
                alert.addOkButton { dialog, _ ->
                    dialog.dismiss()
                    if (activity is PurchaseActivity) {
                        activity.finish()
                    }
                }
                alert.enqueue()
            }
        }
    }

    companion object {
        private const val PENDING_GIFTS_KEY = "PENDING_GIFTS_DATED"
        private var pendingGifts: MutableMap<String, Pair<Date, String>> = HashMap()
        private var preferences: SharedPreferences? = null

        fun addGift(sku: String, userID: String) {
            pendingGifts[sku] = Pair(Date(), userID)
            savePendingGifts()
        }

        private fun removeGift(sku: String?): Pair<Date, String>? {
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
}

suspend fun retryUntil(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000, // 1 second
    factor: Double = 2.0,
    block: suspend () -> Boolean
) {
    var currentDelay = initialDelay
    repeat(times - 1) {
        if (block()) return
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
}
