package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Customization
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper


expect class Purchases {

    var userId: String?

    var customizations: RealmListWrapper<Customization>
    internal var user: User?
    var plan: SubscriptionPlan?
}
