package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class QuestRageStrike() : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var key = ""
    var wasHit = false


    constructor(key: String, wasHit: Boolean) : this() {
        this.key = key
        this.wasHit = wasHit
    }
}

