package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.asFlow
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
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
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.PurchaseActivity
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.IAPGift
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import com.habitrpg.common.habitica.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PurchaseHandler(
    private val context: Context,
    private val apiClient: ApiClient,
    private val userViewModel: MainUserViewModel,
    private val configManager: AppConfigManager
) : PurchasesUpdatedListener, PurchasesResponseListener {
    private val billingClient =
        BillingClient.newBuilder(context).setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    private val inventoryManager = InventoryManager(billingClient)

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        purchases?.let { processPurchases(result, it) }
    }

    override fun onQueryPurchasesResponse(
        result: BillingResult,
        purchases: MutableList<Purchase>
    ) {
        processPurchases(result, purchases)
    }

    private fun processPurchases(
        result: BillingResult,
        purchases: List<Purchase>
    ) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val mostRecentSub = findMostRecentSubscription(purchases)
                MainScope().launchCatching {
                    userViewModel.user.asFlow()
                        .filterNotNull().take(1).collect {
                            val plan = it.purchased!!.plan
                            for (purchase in purchases) {
                                val product = HabiticaProduct.forSku(purchase.products.firstOrNull() ?: "") ?: continue
                                if (plan?.isActive == true &&
                                    HabiticaProduct.allSubscriptionTypes.contains(product)
                                ) {
                                    if (((plan.dateTerminated != null) == purchase.isAutoRenewing) ||
                                        mostRecentSub?.orderId != purchase.orderId ||
                                        purchase.purchaseToken == plan.customerId
                                    ) {
                                        continue
                                    }
                                }
                                handle(purchase)
                            }
                        }
                }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
                    for (purchase in purchases) {
                        consume(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                return
            }

            else -> {
                CrashReporter.recordException(Throwable(result.debugMessage))
            }
        }
    }

    init {
        startListening()
    }

    fun startListening() {
        if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTING ||
            billingClient.connectionState == BillingClient.ConnectionState.CONNECTED
        ) {
            // Don't connect again if it's already connected
            return
        }
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            MainScope().launchCatching {
                                queryPurchases()
                            }
                        }
                    }
                }
                override fun onBillingServiceDisconnected() {
                }
            }
        )
    }

    fun stopListening() {
        billingClient.endConnection()
    }

    suspend fun queryPurchases() {
        retryUntil {
            billingClient.isReady
        }
        val subResponse =
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        processPurchases(subResponse.billingResult, subResponse.purchasesList)
        val iapResponse =
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        processPurchases(iapResponse.billingResult, iapResponse.purchasesList)
    }

    suspend fun loadGryphatriceProduct() = inventoryManager.loadInAppProduct(HabiticaProduct.JUBILANT_GRYPHATRICE)
    suspend fun loadGemProducts() = inventoryManager.loadGemProducts()
    suspend fun loadSubscriptionProducts() = inventoryManager.loadSubscriptionProducts()
    suspend fun loadInAppProduct(product: HabiticaProduct) = inventoryManager.loadInAppProduct(product)

    suspend fun loadGiftSubscriptionProducts() = inventoryManager.loadGiftSubscriptionProducts()
    private fun isSubscriptionUpgrade(
        oldPurchase: Purchase,
        newSkuDetails: ProductDetails
    ): Boolean {
        if (oldPurchase.products.firstOrNull() == null) return false
        val oldSku = oldPurchase.products.first()
        val oldProduct = HabiticaProduct.forSku(oldSku) ?: return false
        val newProduct = HabiticaProduct.forSku(newSkuDetails.productId) ?: return false
        if (!HabiticaProduct.allSubscriptionTypes.contains(oldProduct) ||
            !HabiticaProduct.allSubscriptionTypes.contains(newProduct)
        ) {
            return false
        }
        val oldDuration = oldProduct.getSubscriptionDuration()
        val newDuration = newProduct.getSubscriptionDuration()
        return newDuration > oldDuration
    }

    private fun getReplacementMode(
        oldPurchase: Purchase,
        newSkuDetails: ProductDetails
    ): Int {
        return if (isSubscriptionUpgrade(oldPurchase, newSkuDetails)) {
            BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams.ReplacementMode.CHARGE_FULL_PRICE
        } else {
            BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams.ReplacementMode.DEFERRED
        }
    }

    suspend fun purchase(
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
        var productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(skuDetails)
        skuDetails.subscriptionOfferDetails?.first()?.offerToken?.let { offerToken ->
            productDetailsParams = productDetailsParams.setOfferToken(offerToken)
        }
        var flowParams = BillingFlowParams.newBuilder()
                .setObfuscatedAccountId(userViewModel.userID)

        if (skuDetails.productType == BillingClient.ProductType.SUBS) {
            val existingSub = checkForSubscription()
            if (existingSub != null) {
                productDetailsParams = productDetailsParams.setSubscriptionProductReplacementParams(
                    BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams.newBuilder()
                        .setOldProductId(existingSub.purchaseToken)
                        .setReplacementMode(getReplacementMode(existingSub, skuDetails))
                        .build()
                )
                flowParams = flowParams.setSubscriptionUpdateParams(
                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                        .setOldPurchaseToken(existingSub.purchaseToken)
                        .build()
                )
            }
        }

        flowParams = flowParams.setProductDetailsParamsList(
                listOf(productDetailsParams.build())
            )
        billingClient.launchBillingFlow(activity, flowParams.build())
    }

    private suspend fun consume(
        purchase: Purchase,
        retries: Int = 4
    ) {
        retryUntil { billingClient.isReady }
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val result = billingClient.consumePurchase(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK && retries > 0) {
            delay(500)
            consume(purchase, retries - 1)
        } else if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            // Throw an error to continue the flow
            throw Exception("Failed to consume purchase after multiple attempts")
        } else {
            userViewModel.userRepository.retrieveUser(false, true)
        }
    }

    private var processedPurchases = mutableSetOf<String>()

    private fun handle(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED ||
            processedPurchases.contains(purchase.orderId)
        ) {
            return
        }
        purchase.orderId?.let { processedPurchases.add(it) }
        val sku = purchase.products.firstOrNull()
        val product = HabiticaProduct.forSku(sku ?: "") ?: return
        when {
            product == HabiticaProduct.JUBILANT_GRYPHATRICE -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        apiClient.validatePurchase(validationRequest)
                        processedPurchase()
                        val gift = removeGift(sku)
                        withContext(Dispatchers.IO) {
                            consume(purchase)
                        }
                        displayGryphatriceConfirmationDialog(gift?.third)
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }

            HabiticaProduct.allGemTypes.contains(product) -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        val response = apiClient.validatePurchase(validationRequest)
                        processedPurchase()
                        val gift = removeGift(sku)
                        withContext(Dispatchers.IO) {
                            consume(purchase)
                        }
                        if (response != null) {
                            displayConfirmationDialog(purchase, gift?.second, gift?.third)
                        }
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }

            HabiticaProduct.allSubscriptionNoRenewTypes.contains(product) -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        val response = apiClient.validateNoRenewSubscription(validationRequest)
                        processedPurchase()
                        val gift = removeGift(sku)
                        withContext(Dispatchers.IO) {
                            consume(purchase)
                        }
                        if (response != null) {
                            displayConfirmationDialog(purchase, gift?.second, gift?.third)
                        }
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }

            HabiticaProduct.allSubscriptionTypes.contains(product) -> {
                val validationRequest = buildValidationRequest(purchase)
                MainScope().launchCatching {
                    try {
                        val response = apiClient.validateSubscription(validationRequest)
                        processedPurchase()
                        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
                            acknowledgePurchase(purchase)
                        }
                        if (response != null) {
                            displayConfirmationDialog(purchase)
                        }
                    } catch (throwable: Throwable) {
                        handleError(throwable, purchase)
                    }
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(
        purchase: Purchase,
        retries: Int = 4
    ) {
        val params =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val response = billingClient.acknowledgePurchase(params)
        if (response.responseCode != BillingClient.BillingResponseCode.OK) {
            delay(500)
            acknowledgePurchase(purchase, retries - 1)
        }
    }

    private fun processedPurchase() {
        MainScope().launch(ExceptionHandler.coroutine()) {
            userViewModel.userRepository.retrieveUser(withTasks = false,
                forced = true)
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

    private fun handleError(
        throwable: Throwable,
        purchase: Purchase
    ) {
        when (throwable) {
            is HttpException -> {
                if (throwable.code() == 401) {
                    val res = apiClient.getErrorResponse(throwable)
                    if (res.message != null && res.message == "RECEIPT_ALREADY_USED") {
                        processedPurchase()
                        removeGift(purchase.products.firstOrNull())
                        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
                            consume(purchase)
                        }
                        return
                    }
                }
            }

            else -> {
                // Handles other potential errors such as IOException or an exception
                // thrown by billingClient.consumePurchase method that is not handled
                CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
                    consume(purchase)
                }
            }
        }
        processedPurchases.remove(purchase.orderId)
        CrashReporter.recordException(throwable)
    }

    suspend fun checkForSubscription(): Purchase? {
        val result =
            withContext(Dispatchers.IO) {
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

    private var alreadyTriedCancellation = false

    suspend fun cancelSubscription(): User? {
        if (alreadyTriedCancellation) return null
        alreadyTriedCancellation = true
        apiClient.cancelSubscription()
        return userViewModel.userRepository.retrieveUser(false, true)
    }

    private var isSaleGemPurchase = false

    private val displayedConfirmations = mutableListOf<String>()

    private fun displayConfirmationDialog(
        purchase: Purchase,
        giftedToID: String? = null,
        giftedTo: String? = null
    ) {
        if (displayedConfirmations.contains(purchase.orderId)) {
            return
        }
        purchase.orderId?.let { displayedConfirmations.add(it) }
        CoroutineScope(Dispatchers.Main).launchCatching {
            val application =
                (context as? HabiticaBaseApplication)
                    ?: (context.applicationContext as? HabiticaBaseApplication) ?: return@launchCatching
            val sku = purchase.products.firstOrNull() ?: return@launchCatching
            val product = HabiticaProduct.forSku(sku) ?: return@launchCatching
            var title = context.getString(R.string.successful_purchase_generic)
            val message =
                when {
                    HabiticaProduct.allSubscriptionNoRenewTypes.contains(product) -> {
                        title = context.getString(R.string.gift_confirmation_title)
                        context.getString(
                            if (configManager.activePromo()?.identifier == "g1g1" && giftedToID != userViewModel.user.value?.id) {
                                R.string.gift_confirmation_text_sub_g1g1
                            } else {
                                R.string.gift_confirmation_text_sub
                            },
                            giftedTo,
                            product.getSubscriptionDuration().toString()
                        )
                    }

                    HabiticaProduct.allSubscriptionTypes.contains(product) -> {
                        if (sku == HabiticaProduct.SUBSCRIPTION_1_MONTH.sku) {
                            context.getString(R.string.subscription_confirmation)
                        } else {
                            context.getString(
                                R.string.subscription_confirmation_multiple,
                                product.getSubscriptionDuration().toString()
                            )
                        }
                    }

                    HabiticaProduct.allGemTypes.contains(product) && giftedTo != null -> {
                        title = context.getString(R.string.gift_confirmation_title)
                        context.getString(
                            R.string.gift_confirmation_text_gems_new,
                            giftedTo,
                            product.getGemAmount(isSaleGemPurchase).toString()
                        )
                    }

                    HabiticaProduct.allGemTypes.contains(product) && giftedTo == null -> {
                        context.getString(R.string.gem_purchase_confirmation, product.getGemAmount(isSaleGemPurchase).toString())
                    }

                    else -> null
                }
            application.currentActivity?.get()?.let { activity ->
                val alert = HabiticaAlertDialog(activity)
                alert.setTitle(title)
                message?.let { alert.setMessage(it) }
                alert.addOkButton { dialog, _ ->
                    dialog.dismiss()
                    if (giftedTo != null) {
                        if (activity is PurchaseActivity) {
                            activity.finish()
                        }
                    }
                }
                alert.enqueue()
            }
        }
    }

    private fun displayGryphatriceConfirmationDialog(
        giftedTo: String? = null
    ) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            val application =
                (context as? HabiticaBaseApplication)
                    ?: (context.applicationContext as? HabiticaBaseApplication) ?: return@launch
            val title = context.getString(R.string.successful_purchase_generic)
            val message =
                if (giftedTo != null) {
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
                }
                alert.enqueue()
            }
        }
    }

    companion object {
        private const val PENDING_GIFTS_KEY = "PENDING_GIFTS_DATED"
        private var pendingGifts: MutableMap<String, Triple<Date, String, String>> = HashMap()
        private var preferences: SharedPreferences? = null

        fun addGift(
            sku: String,
            userID: String,
            username: String
        ) {
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
