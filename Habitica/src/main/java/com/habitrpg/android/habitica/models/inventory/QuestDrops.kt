package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by phillip on 25.07.17.
 */
open class QuestDrops : RealmObject() {
    @PrimaryKey
    var key: String? = null
    set(value) {
        field = value
        items?.forEach { it.questKey = key }
    }
    var gp = 0
    var exp = 0
    var unlock: String? = null
    var items: RealmList<QuestDropItem>? = null
    set(value) {
        field = value
        items?.forEach { it.questKey = key }
    }
}