package com.habitrpg.android.habitica.helpers

object PurchaseTypes {
    const val JUBILANT_GRYPHATRICE = "com.habitrpg.android.habitica.iap.pets.gryphatrice_jubilant"
    const val PURCHASE_4_GEMS = "com.habitrpg.android.habitica.iap.4gems"
    const val PURCHASE_21_GEMS = "com.habitrpg.android.habitica.iap.21gems"
    const val PURCHASE_42_GEMS = "com.habitrpg.android.habitica.iap.42gems"
    const val PURCHASE_84_GEMS = "com.habitrpg.android.habitica.iap.84gems"
    val allGemTypes = listOf(PURCHASE_4_GEMS, PURCHASE_21_GEMS, PURCHASE_42_GEMS, PURCHASE_84_GEMS)
    const val SUBSCRIPTION_1_MONTH = "com.habitrpg.android.habitica.subscription.1month"
    const val SUBSCRIPTION_3_MONTH = "com.habitrpg.android.habitica.subscription.3month"
    const val SUBSCRIPTION_6_MONTH = "com.habitrpg.android.habitica.subscription.6month"
    const val SUBSCRIPTION_12_MONTH = "com.habitrpg.android.habitica.subscription.12month"
    val allSubscriptionTypes =
        mutableListOf(
            SUBSCRIPTION_1_MONTH,
            SUBSCRIPTION_3_MONTH,
            SUBSCRIPTION_6_MONTH,
            SUBSCRIPTION_12_MONTH
        )
    const val SUBSCRIPTION_1_MONTH_NORENEW =
        "com.habitrpg.android.habitica.norenew_subscription.1month"
    const val SUBSCRIPTION_3_MONTH_NORENEW =
        "com.habitrpg.android.habitica.norenew_subscription.3month"
    const val SUBSCRIPTION_6_MONTH_NORENEW =
        "com.habitrpg.android.habitica.norenew_subscription.6month"
    const val SUBSCRIPTION_12_MONTH_NORENEW =
        "com.habitrpg.android.habitica.norenew_subscription.12month"
    var allSubscriptionNoRenewTypes =
        listOf(
            SUBSCRIPTION_1_MONTH_NORENEW,
            SUBSCRIPTION_3_MONTH_NORENEW,
            SUBSCRIPTION_6_MONTH_NORENEW,
            SUBSCRIPTION_12_MONTH_NORENEW
        )
}
