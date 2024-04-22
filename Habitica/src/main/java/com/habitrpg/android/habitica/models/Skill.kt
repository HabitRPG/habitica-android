package com.habitrpg.android.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Skill : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String = ""
    var text: String = ""
    var notes: String? = null
    var target: String? = null
    var habitClass: String? = null
    var mana: Int? = null
    var lvl: Int? = null
}
