package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestMember : RealmObject() {
    @PrimaryKey
    var key: String? = null
    var isParticipating: Boolean? = null
}