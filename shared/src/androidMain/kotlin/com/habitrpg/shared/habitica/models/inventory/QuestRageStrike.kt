package com.habitrpg.shared.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class QuestRageStrike actual constructor() : RealmObject() {

    @PrimaryKey
    actual var key = ""
    actual var wasHit = false


    actual constructor(key: String, wasHit: Boolean): this() {
        this.key = key
        this.wasHit = wasHit
    }
}

