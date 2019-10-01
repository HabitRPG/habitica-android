package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class SubscriptionPlanConsecutive : RealmObject() {

    @PrimaryKey
    actual var customerId: String? = null

    internal actual var subscriptionPlan: SubscriptionPlan? = null
    actual var trinkets: Int = 0
    actual var gemCapExtra: Int = 0
    actual var offset: Int = 0
    actual var count: Int = 0
}
