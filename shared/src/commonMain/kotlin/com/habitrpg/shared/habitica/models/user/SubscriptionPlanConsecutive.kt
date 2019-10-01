package com.habitrpg.shared.habitica.models.user


expect open class SubscriptionPlanConsecutive {

    var customerId: String?
    internal var subscriptionPlan: SubscriptionPlan?
    var trinkets: Int
    var gemCapExtra: Int
    var offset: Int
    var count: Int
}
