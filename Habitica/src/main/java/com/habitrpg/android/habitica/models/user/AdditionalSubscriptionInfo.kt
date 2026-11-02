package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class AdditionalSubscriptionInfo : RealmObject() {
    var data: GoogleSubscriptionData? = null
}
