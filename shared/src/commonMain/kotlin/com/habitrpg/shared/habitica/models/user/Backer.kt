package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject

open class Backer : NativeRealmObject() {
    var id: String? = null
    var npc: String? = null
    var tier: Int? = null
}