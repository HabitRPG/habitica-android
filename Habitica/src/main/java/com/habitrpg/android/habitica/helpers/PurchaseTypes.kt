package com.habitrpg.android.habitica.helpers

object PurchaseTypes {
    const val JubilantGrphatrice = "com.habitrpg.android.habitica.iap.pets.gryphatrice_jubilant"
    const val Purchase4Gems = "com.habitrpg.android.habitica.iap.4gems"
    const val Purchase21Gems = "com.habitrpg.android.habitica.iap.21gems"
    const val Purchase42Gems = "com.habitrpg.android.habitica.iap.42gems"
    const val Purchase84Gems = "com.habitrpg.android.habitica.iap.84gems"
    val allGemTypes = listOf(Purchase4Gems, Purchase21Gems, Purchase42Gems, Purchase84Gems)
    const val Subscription1Month = "com.habitrpg.android.habitica.subscription.1month"
    const val Subscription3Month = "com.habitrpg.android.habitica.subscription.3month"
    const val Subscription6Month = "com.habitrpg.android.habitica.subscription.6month"
    const val Subscription12Month = "com.habitrpg.android.habitica.subscription.12month"
    val allSubscriptionTypes = mutableListOf(
        Subscription1Month,
        Subscription3Month,
        Subscription6Month,
        Subscription12Month
    )
    const val Subscription1MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.1month"
    const val Subscription3MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.3month"
    const val Subscription6MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.6month"
    const val Subscription12MonthNoRenew = "com.habitrpg.android.habitica.norenew_subscription.12month"
    var allSubscriptionNoRenewTypes = listOf(
        Subscription1MonthNoRenew,
        Subscription3MonthNoRenew,
        Subscription6MonthNoRenew,
        Subscription12MonthNoRenew
    )
}
