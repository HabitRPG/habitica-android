package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestColors : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String? = null
    var dark: String? = null
    var medium: String? = null
    var light: String? = null
    var extralight: String? = null
}
