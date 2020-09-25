package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SubscriptionPlanConsecutive : RealmObject() {
    @PrimaryKey
    var customerId: String? = null
    var subscriptionPlan: SubscriptionPlan? = null
    var trinkets = 0
    var gemCapExtra = 0
    var offset = 0
    var count = 0
}