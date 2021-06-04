package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Purchases : RealmObject(), BaseObject {
    @JvmField
    var customizations: RealmList<OwnedCustomization>? = null
    var user: User? = null
    var plan: SubscriptionPlan? = null
}