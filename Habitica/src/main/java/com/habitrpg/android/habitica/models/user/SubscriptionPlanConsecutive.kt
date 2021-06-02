package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SubscriptionPlanConsecutive : RealmObject(), BaseObject {
    @PrimaryKey
    var customerId: String? = null
    var subscriptionPlan: SubscriptionPlan? = null
    var trinkets = 0
    var gemCapExtra = 0
    var offset = 0
    var count = 0
}