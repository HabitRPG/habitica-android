package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class GoogleSubscriptionData : RealmObject() {
    var orderId: String? = null
    var productId: String? = null
}
