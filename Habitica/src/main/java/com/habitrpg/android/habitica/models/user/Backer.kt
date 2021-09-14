package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Backer : RealmObject(), BaseObject {
    var npc: String? = null
    var tier: Int? = null
}
