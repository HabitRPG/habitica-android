package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Customization
import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList


expect class Purchases {

    var userId: String?

    var customizations: NativeRealmList<Customization>
    internal var user: User?
    var plan: SubscriptionPlan?
}
