package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestCollect : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String? = null
    var text: String? = null
    var count = 0
}