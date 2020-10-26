package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class QuestProgress : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var id: String? = null
    var key: String? = null
    var hp: Double = 0.0
    var rage: Double = 0.0
    var collectedItems: Int = 0
    var collect: NativeList<QuestProgressCollect>? = null
    var down: Float = 0.0f
    var up: Float = 0.0f
}
