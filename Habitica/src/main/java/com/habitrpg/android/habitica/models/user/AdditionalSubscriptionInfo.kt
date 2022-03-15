package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class AdditionalSubscriptionInfo: RealmObject() {
    var data: GoogleSubscriptionData? = null
}