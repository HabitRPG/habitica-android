package com.habitrpg.android.habitica.models.social

import io.realm.RealmObject

open class Backer : RealmObject() {
    var level: Int = 0
    var npc: String? = null
}
