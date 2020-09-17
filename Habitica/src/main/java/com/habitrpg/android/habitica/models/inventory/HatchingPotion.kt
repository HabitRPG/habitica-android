package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class HatchingPotion : RealmObject(), Item {
    @PrimaryKey
    override var key: String = ""
    override var text: String = ""
    var notes: String? = null
    override var value: Int = 0
    var limited: Boolean? = null
    var premium: Boolean? = null
    override val type: String
        get() = "hatchingPotions"
}