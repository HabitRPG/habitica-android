package com.habitrpg.android.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Achievement : RealmObject() {
    @PrimaryKey
    var key: String? = null
    var type: String? = null
    var title: String? = null
    var text: String? = null
    var icon: String? = null
    var category: String? = null
    var earned: Boolean = false
    var index: Int = 0
    var optionalCount: Int? = null
}
