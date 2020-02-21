package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class QuestProgressCollect : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var key: String? = null

    var count: Int = 0
}
