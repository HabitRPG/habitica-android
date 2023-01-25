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
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.activities.PurchaseActivity
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.models.IAPGift
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import com.habitrpg.common.habitica.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PurchaseHandler(
    private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val apiClient: ApiClient,
    private val userViewModel: MainUserViewModel
) : PurchasesUpdatedListener, PurchasesResponseListener {
    private val billingClient =
        BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.let { processPurchases(result, it) }
    }

    override fun onQueryPurchasesResponse(result: BillingResult, purchases: MutableList<Purchase>) {
        processPurchases(result, purchases)
    }

    private fun processPurchases(result: BillingResult, purchases: List<Purchase>) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val mostRecentSub = findMostRecentSubscription(purchases)
                val plan = userViewModel.user.value?.purchased?.plan
                for (purchase in purchases) {
                    if (plan?.isActive == true && PurchaseTypes.allSubscriptionTypes.contains(
                            purchase.products.firstOrNull()
                        )
                    ) {
                        if ((plan.additionalData?.data?.orderId == purchase.orderId && ((plan.dateTerminated != null) == purchase.isAutoRenewing)) || mostRecentSub?.orderId != purchase.orderId) {
                            return
                        }
                    }
                    handle(purchase)
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                return
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
                    for (purchase in purchases) {
                        consume(purchase)
                    }
                }
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                startListening()
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
        UNINITIALIZED, READY, UNAVAILABLE, DISCONNECTED, CONNECTING;

        val canMaybePurchase: Boolean
            get() {
                return this == UNINITIALIZED || this == READY || this == CONNECTING
            }
    }

    fun startListening() {
        if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTING
            || billingClient.connectionState == BillingClient.ConnectionState.CONNECTED
            || billingClientState == BillingClientState.UNAVAILABLE) {
            // Don't connect again if it's already connected
            return
        }
        billingClientState = BillingClientState.CONNECTING
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClientState = BillingClientState.READY
                    MainScope().launchCatching {
                        queryPurchases()
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                    startListening()
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_TIMEOUT) {
                    CoroutineScope(Dispatchers.IO).launchCatching {
                        // try again after 30 seconds
                        delay(30.seconds)
                        startListening()
                    }
                } else {
                    billingClientState = BillingClientState.UNAVAILABLE
                }
            }

            override fun onBillingServiceDisconnected() {
                billingClientState = BillingClientState.DISCONNECTED
                startListening()
            }
        })
    }

    fun stopListening() {
        billingClient.endConnection()
    }

    suspend fun queryPurchases() {
        retryUntil {
            if (billingClientState == BillingClientState.DISCONNECTED) {
                startListening()
            }
            billingClientState.canMaybePurchase && billingClient.isReady
        }
        val subResponse = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        processPurchases(subResponse.billingResult, subResponse.purchasesList)
        val iapResponse = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        processPurchases(iapResponse.billingResult, iapResponse.purchasesList)
    }

    suspend fun getGryphatriceSKU() =
        getSKU(BillingClient.ProductType.INAPP, PurchaseTypes.JubilantGrphatrice)

    suspend fun getAllGemSKUs() =
        getSKUs(BillingClient.ProductType.INAPP, PurchaseTypes.allGemTypes)

    suspend fun getAllSubscriptionProducts() =
        getSKUs(BillingClient.ProductType.SUBS, PurchaseTypes.allSubscriptionTypes)

    suspend fun getAllGiftSubscriptionProducts() =
        getSKUs(BillingClient.ProductType.INAPP, PurchaseTypes.allSubscriptionNoRenewTypes)

    suspend fun getInAppPurchaseSKU(identifier: String) =
        getSKU(BillingClient.ProductType.INAPP, identifier)

    private suspend fun getSKUs(type: String, identifiers: List<String>) =
        loadInventory(type, identifiers) ?: emptyList()

    private suspend fun getSKU(type: String, identifier: String): ProductDetails? {
        val inventory = loadInventory(type, listOf(identifier))
        return inventory?.firstOrNull()
    }

    private suspend fun loadInventory(type: String, skus: List<String>): List<ProductDetails>? {
        retryUntil {
            if (billingClientState == BillingClientState.DISCONNECTED) {
                startListening()
            }
            billingClientState.canMaybePurchase && billingClient.isReady
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(skus.map {
                Product.newBuilder().setProductId(it).setProductType(type).build()
            }).build()
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }
        return skuDetailsResult.productDetailsList
    }

    fun purchase(
        activity: Activity,
        skuDetails: ProductDetails,
        recipient: String? = null,
        recipientUsername: String? = null,
        isSaleGemPurchase: Boolean = false
    ) {
        this.isSaleGemPurchase = isSaleGemPurchase
        recipient?.let {
            addGift(skuDetails.productId, it, recipientUsername ?: it)
        }
        val flowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(skuDetails).map {
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(skuDetails).setOfferToken(
                            skuDetails.subscriptionOfferDetails?.first()?.offerToken ?: ""
                        ).build()
                }).build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    private suspend fun consume(purchase: Purchase, retries: Int = 4) {
        retryUntil { billingClientState.canMaybePurchase && billingClient.isReady }
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val result = billingClient.consumePurchase(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            delay(500)
            consume(purchase, retries - 1)
        } else {
            userViewModel.userRepository.retrieveUser(false, true)
        }
    }

    private fun handle(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
        }
        val sku = purchase.products.firstOrNull()
        when {
            sku == PurchaseTypes.JubilantGrphatrice -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        apiClient.validatePurchase(validationRequest)
                        processedPurchase(purchase)
                        val gift = removeGift(sku)
                        withContext(Dispatchers.IO) {
                            consume(purchase)
                        }
                        displayGryphatriceConfirmationDialog(purchase, gift?.third)
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }

            PurchaseTypes.allGemTypes.contains(sku) -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        apiClient.validatePurchase(validationRequest)
                        processedPurchase(purchase)
                        val gift = removeGift(sku)
                        withContext(Dispatchers.IO) {
                            consume(purchase)
                        }
                        displayConfirmationDialog(purchase, gift?.third)
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }

            PurchaseTypes.allSubscriptionNoRenewTypes.contains(sku) -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        apiClient.validateNoRenewSubscription(validationRequest)
                        processedPurchase(purchase)
                        val gift = removeGift(sku)
                        withContext(Dispatchers.IO) {
                            consume(purchase)
                        }
                        displayConfirmationDialog(purchase, gift?.third)
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }

            PurchaseTypes.allSubscriptionTypes.contains(sku) -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        apiClient.validateSubscription(validationRequest)
                        processedPurchase(purchase)
                        analyticsManager.logEvent("user_subscribed", bundleOf(Pair("sku", sku)))
                        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
                            acknowledgePurchase(purchase)
                        }
                        displayConfirmationDialog(purchase)
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase, retries: Int = 4) {
        val params =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val response = billingClient.acknowledgePurchase(params)
        if (response.responseCode != BillingClient.BillingResponseCode.OK) {
            delay(500)
            acknowledgePurchase(purchase, retries - 1)
        }
    }

    private fun processedPurchase(purchase: Purchase) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            userViewModel.userRepository.retrieveUser(false, true)
        }
    }

    private fun buildValidationRequest(purchase: Purchase): PurchaseValidationRequest {
        val validationRequest = PurchaseValidationRequest()
        validationRequest.sku = purchase.products.firstOrNull()
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
                    removeGift(purchase.products.firstOrNull())
                    CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
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
            val params =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            billingClient.queryPurchasesAsync(params)
        }
        val fallback: Purchase? = null
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            return findMostRecentSubscription(result.purchasesList)
        }
        return fallback
    }

    private fun findMostRecentSubscription(purchasesList: List<Purchase>): Purchase? {
        val purchases =
            purchasesList.filter { it.isAcknowledged }.sortedByDescending { it.purchaseTime }
        var fallback: Purchase? = null
        // If there is a subscription that is still active, prioritise that. Otherwise return the most recent one.
        for (purchase in purchases) {
            if (purchase.isAutoRenewing) {
                return purchase
            } else if (!purchase.isAutoRenewing && fallback == null) {
                fallback = purchase
            }
        }
        return fallback
    }

    suspend fun cancelSubscription(): User? {
        apiClient.cancelSubscription()
        return userViewModel.userRepository.retrieveUser(false, true)
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

    private var isSaleGemPurchase = false

    private fun gemAmountString(sku: String): String {
        if (isSaleGemPurchase) {
            isSaleGemPurchase = false
            return when (sku) {
                PurchaseTypes.Purchase4Gems -> "5"
                PurchaseTypes.Purchase21Gems -> "30"
                PurchaseTypes.Purchase42Gems -> "60"
                PurchaseTypes.Purchase84Gems -> "125"
                else -> ""
            }
        } else {
            return when (sku) {
                PurchaseTypes.Purchase4Gems -> "4"
                PurchaseTypes.Purchase21Gems -> "21"
                PurchaseTypes.Purchase42Gems -> "42"
                PurchaseTypes.Purchase84Gems -> "84"
                else -> ""
            }
        }
    }

    private val displayedConfirmations = mutableListOf<String>()

    private fun displayConfirmationDialog(purchase: Purchase, giftedTo: String? = null) {
        if (displayedConfirmations.contains(purchase.orderId)) {
            return
        }
        displayedConfirmations.add(purchase.orderId)
        CoroutineScope(Dispatchers.Main).launchCatching {
            val application = (context as? HabiticaBaseApplication)
                ?: (context.applicationContext as? HabiticaBaseApplication) ?: return@launchCatching
            val sku = purchase.products.firstOrNull() ?: return@launchCatching
            var title = context.getString(R.string.successful_purchase_generic)
            val message = when {
                PurchaseTypes.allSubscriptionNoRenewTypes.contains(sku) -> {
                    title = context.getString(R.string.gift_confirmation_title)
                    context.getString(
                        R.string.gift_confirmation_text_sub, giftedTo, durationString(sku)
                    )
                }

                PurchaseTypes.allSubscriptionTypes.contains(sku) -> {
                    if (sku == PurchaseTypes.Subscription1Month) {
                        context.getString(R.string.subscription_confirmation)
                    } else {
                        context.getString(
                            R.string.subscription_confirmation_multiple, durationString(sku)
                        )
                    }
                }

                PurchaseTypes.allGemTypes.contains(sku) && giftedTo != null -> {
                    title = context.getString(R.string.gift_confirmation_title)
                    context.getString(
                        R.string.gift_confirmation_text_gems_new, giftedTo, gemAmountString(sku)
                    )
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

    private fun displayGryphatriceConfirmationDialog(purchase: Purchase, giftedTo: String? = null) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            val application = (context as? HabiticaBaseApplication)
                ?: (context.applicationContext as? HabiticaBaseApplication) ?: return@launch
            val title = context.getString(R.string.successful_purchase_generic)
            val message = if (giftedTo != null) {
                context.getString(R.string.jubilant_gryphatrice_confirmation_gift)
            } else {
                context.getString(R.string.jubilant_gryphatrice_confirmation)
            }
            application.currentActivity?.get()?.let { activity ->
                val alert = HabiticaAlertDialog(activity)
                alert.setTitle(title)
                alert.setMessage(message)
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
        private var pendingGifts: MutableMap<String, Triple<Date, String, String>> = HashMap()
        private var preferences: SharedPreferences? = null

        fun addGift(sku: String, userID: String, username: String) {
            pendingGifts[sku] = Triple(Date(), userID, username)
            savePendingGifts()
        }

        private fun removeGift(sku: String?): Triple<Date, String, String>? {
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
    times: Int = Int.MAX_VALUE, initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000, // 1 second
    factor: Double = 2.0, block: suspend () -> Boolean
) {
    var currentDelay = initialDelay
    repeat(times - 1) {
        if (block()) return
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
}
