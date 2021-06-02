package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject

open class Backer : RealmObject(), BaseObject {
    var id: String? = null
    var npc: String? = null
    var tier: Int? = null
}