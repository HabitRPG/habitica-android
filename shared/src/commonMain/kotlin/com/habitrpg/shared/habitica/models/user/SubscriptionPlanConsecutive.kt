package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class SubscriptionPlanConsecutive : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var customerId: String? = null

    internal var subscriptionPlan: SubscriptionPlan? = null
    var trinkets: Int = 0
    var gemCapExtra: Int = 0
    var offset: Int = 0
    var count: Int = 0
}
