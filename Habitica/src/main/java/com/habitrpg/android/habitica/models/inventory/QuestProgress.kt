package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestProgress : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var key: String? = null
    var hp: Double = 0.0
    var rage: Double = 0.0
    var collectedItems: Int = 0
    var collect: RealmList<QuestProgressCollect>? = null
    var down: Float = 0.0f
    var up: Float = 0.0f
}
