package com.habitrpg.android.habitica.helpers

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryManager(
    private val billingClient: BillingClient
) {
    private suspend fun loadInventory(
        type: String,
        skus: List<HabiticaProduct>
    ): List<ProductDetails>? {
        val params =
            QueryProductDetailsParams.newBuilder().setProductList(
                skus.map {
                    Product.newBuilder().setProductId(it.sku).setProductType(type).build()
                }
            ).build()
        val skuDetailsResult =
            withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params)
            }
        if (skuDetailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e("PurchaseHandler", "Failed to load inventory: ${skuDetailsResult.billingResult.debugMessage}")
            CrashReporter.recordException(
                Throwable(
                    "Failed to load inventory: ${skuDetailsResult.billingResult.debugMessage}"
                )
            )
            return null
        }
        return skuDetailsResult.productDetailsList
    }

    private suspend fun loadProducts(type: String, skus: List<HabiticaProduct>): List<ProductDetails> {
        return loadInventory(type, skus) ?: emptyList()
    }

    suspend fun loadGemProducts() =
        loadProducts(BillingClient.ProductType.INAPP, HabiticaProduct.allGemTypes)

    suspend fun loadSubscriptionProducts() =
        loadProducts(BillingClient.ProductType.SUBS, HabiticaProduct.allSubscriptionTypes)

    suspend fun loadGiftSubscriptionProducts() =
        loadProducts(BillingClient.ProductType.SUBS, HabiticaProduct.allSubscriptionNoRenewTypes)

    suspend fun loadInAppProduct(identifier: HabiticaProduct) =
        loadProducts(BillingClient.ProductType.INAPP, listOf(identifier)).firstOrNull()
}