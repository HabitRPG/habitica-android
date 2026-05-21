package com.habitrpg.android.habitica.helpers

enum class HabiticaProduct(val sku: String) {
    JUBILANT_GRYPHATRICE("com.habitrpg.android.habitica.iap.pets.gryphatrice_jubilant"),
    PURCHASE_4_GEMS("com.habitrpg.android.habitica.iap.4gems"),
    PURCHASE_21_GEMS("com.habitrpg.android.habitica.iap.21gems"),
    PURCHASE_42_GEMS("com.habitrpg.android.habitica.iap.42gems"),
    PURCHASE_84_GEMS("com.habitrpg.android.habitica.iap.84gems"),
    SUBSCRIPTION_1_MONTH("com.habitrpg.android.habitica.subscription.1month"),
    SUBSCRIPTION_3_MONTH("com.habitrpg.android.habitica.subscription.3month"),
    SUBSCRIPTION_6_MONTH("com.habitrpg.android.habitica.subscription.6month"),
    SUBSCRIPTION_12_MONTH("com.habitrpg.android.habitica.subscription.12month"),
    SUBSCRIPTION_1_MONTH_NORENEW("com.habitrpg.android.habitica.norenew_subscription.1month"),
    SUBSCRIPTION_3_MONTH_NORENEW("com.habitrpg.android.habitica.norenew_subscription.3month"),
    SUBSCRIPTION_6_MONTH_NORENEW("com.habitrpg.android.habitica.norenew_subscription.6month"),
    SUBSCRIPTION_12_MONTH_NORENEW("com.habitrpg.android.habitica.norenew_subscription.12month");

    fun getSubscriptionDuration(): Int {
            return when (this) {
                SUBSCRIPTION_1_MONTH, SUBSCRIPTION_1_MONTH_NORENEW -> 1
                SUBSCRIPTION_3_MONTH, SUBSCRIPTION_3_MONTH_NORENEW -> 3
                SUBSCRIPTION_6_MONTH, SUBSCRIPTION_6_MONTH_NORENEW -> 6
                SUBSCRIPTION_12_MONTH, SUBSCRIPTION_12_MONTH_NORENEW -> 12
                else -> 0
            }
        }

    fun getGemAmount(isSaleGemPurchase: Boolean): Int {
        if (isSaleGemPurchase) {
            return when (this) {
                PURCHASE_4_GEMS -> 5
                PURCHASE_21_GEMS -> 30
                PURCHASE_42_GEMS -> 60
                PURCHASE_84_GEMS -> 125
                else -> 0
            }
        } else {
            return when (this) {
                PURCHASE_4_GEMS -> 4
                PURCHASE_21_GEMS -> 21
                PURCHASE_42_GEMS -> 42
                PURCHASE_84_GEMS -> 84
                else -> 0
            }
        }
    }

    companion object {
        val allSubscriptionTypes =
            mutableListOf(
                SUBSCRIPTION_1_MONTH,
                SUBSCRIPTION_3_MONTH,
                SUBSCRIPTION_6_MONTH,
                SUBSCRIPTION_12_MONTH
            )
        var allSubscriptionNoRenewTypes =
            listOf(
                SUBSCRIPTION_1_MONTH_NORENEW,
                SUBSCRIPTION_3_MONTH_NORENEW,
                SUBSCRIPTION_6_MONTH_NORENEW,
                SUBSCRIPTION_12_MONTH_NORENEW
            )
        val allGemTypes =
            listOf(
                PURCHASE_4_GEMS,
                PURCHASE_21_GEMS,
                PURCHASE_42_GEMS,
                PURCHASE_84_GEMS
            )

        fun forSku(sku: String): HabiticaProduct? {
            return entries.firstOrNull { it.sku == sku }
        }
    }
}
