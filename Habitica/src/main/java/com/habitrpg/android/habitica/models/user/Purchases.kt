package com.habitrpg.android.habitica.models.user

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Purchases : RealmObject() {
    @PrimaryKey
    var userId: String? = null
    @JvmField
    var customizations: RealmList<OwnedCustomization>? = null
    var user: User? = null
    var plan: SubscriptionPlan? = null
}