package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class QuestProgressCollect : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String? = null
    var count = 0
}