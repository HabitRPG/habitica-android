package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Purchases : NativeRealmObject() {
    @PrimaryKeyAnnotation
    var userId: String? = null
    var customizations: NativeList<OwnedCustomization>? = null
    var user: User? = null
    var plan: SubscriptionPlan? = null
}
