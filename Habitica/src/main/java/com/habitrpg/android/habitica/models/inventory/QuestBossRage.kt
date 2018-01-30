package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestBossRage : RealmObject() {

    @PrimaryKey
    var key: String? = null

    var title: String? = null

    var description: String? = null

    var value: Double = 0.toDouble()
    var tavern: String? = null
    var stables: String? = null
    var market: String? = null
}
