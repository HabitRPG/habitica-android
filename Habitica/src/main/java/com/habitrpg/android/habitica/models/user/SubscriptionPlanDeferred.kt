package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date

@RealmClass(embedded = true)
open class SubscriptionPlanDeferred : RealmObject(), BaseObject {
    var planId: String? = null
    var deferredUntil: Date? = null
}
