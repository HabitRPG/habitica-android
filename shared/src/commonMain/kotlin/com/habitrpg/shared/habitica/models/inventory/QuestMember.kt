package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class QuestMember : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var key: String? = null

    var isParticipating: Boolean? = null
}
