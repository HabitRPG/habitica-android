package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject

open class Backer : RealmObject() {
    var id: String? = null
    var npc: String? = null
    var tier: Int? = null
}