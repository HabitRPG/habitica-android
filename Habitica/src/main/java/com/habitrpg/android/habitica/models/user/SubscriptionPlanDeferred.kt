@file:UseSerializers(DateSerializer::class)

package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.serialization.DateSerializer
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@RealmClass(embedded = true)
@Serializable
open class SubscriptionPlanDeferred :
    RealmObject(),
    BaseObject {
    var planId: String? = null
    var deferredUntil: Date? = null
}
