package com.habitrpg.shared.habitica.models.inventory

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class QuestProgress : RealmObject() {

    @PrimaryKey
    actual var id: String? = null
    actual var key: String? = null
    actual var hp: Double = 0.0
    actual var rage: Double = 0.0
    actual var collect: RealmList<QuestProgressCollect>? = null
    actual var down: Float = 0.0f
    actual var up: Float = 0.0f
}
