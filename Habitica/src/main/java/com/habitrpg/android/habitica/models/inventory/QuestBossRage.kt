package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestBossRage : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String? = null

    var title: String? = null

    var description: String? = null

    var value: Double = 0.toDouble()
    var stables: String? = null
    var market: String? = null
}
