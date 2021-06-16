package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Food : RealmObject(), Item {
    @PrimaryKey
    override var key: String = ""
    override var text: String = ""
    override var event: ItemEvent? = null
    var notes: String? = null
    override var value: Int = 0
    var target: String? = null
    var article: String? = null
    var canDrop: Boolean? = null
    override val type: String
        get() = "food"
}