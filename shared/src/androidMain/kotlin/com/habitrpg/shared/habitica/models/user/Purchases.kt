package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Customization
import com.habitrpg.shared.habitica.models.user.User

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual class Purchases : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    actual var customizations: RealmList<Customization>
    actual internal var user: User? = null
    actual var plan: SubscriptionPlan? = null
}
