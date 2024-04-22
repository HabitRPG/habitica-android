package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestBoss : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String? = null
        set(value) {
            field = value
            rage?.key = key
        }
    var name: String? = null
    var hp: Int = 0
    var str: Float = 0.toFloat()

    var rage: QuestBossRage? = null

    val hasRage: Boolean
        get() {
            return (rage?.value ?: 0.0) > 0.0
        }
}
