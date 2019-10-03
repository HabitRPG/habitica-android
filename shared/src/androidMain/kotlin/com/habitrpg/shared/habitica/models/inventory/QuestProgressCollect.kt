package com.habitrpg.shared.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class QuestProgressCollect : RealmObject() {

    @PrimaryKey
    actual var key: String? = null

    actual var count: Int = 0
}
