package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.PurchasesSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable(with = PurchasesSerializer::class)
open class Purchases :
    RealmObject(),
    BaseObject {
    @JvmField
    var customizations: RealmList<OwnedCustomization>? = RealmList()
    var user: User? = null
    var plan: SubscriptionPlan? = null
}
