package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.members.Member

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Quest : RealmObject() {
    @PrimaryKey
    var id: String? = null
    set(value) {
        field = value
        progress?.id = value
    }
    var key: String = ""
    set(value) {
        field = value
        progress?.key = key
    }
    var active: Boolean = false
    var leader: String? = null
    var RSVPNeeded: Boolean = false

    var members: RealmList<QuestMember>? = null
    var progress: QuestProgress? = null

    var participants: RealmList<Member>? = null
    var rageStrikes: RealmList<QuestRageStrike>? = null

    var completed: String? = null

    fun hasRageStrikes(): Boolean {
        return rageStrikes?.isNotEmpty() ?: false
    }

    fun addRageStrike(rageStrike: QuestRageStrike) {
        if (rageStrikes == null) {
            rageStrikes = RealmList()
        }
        rageStrikes?.add(rageStrike)
    }

    val activeRageStrikeNumber: Int
    get() {
        return rageStrikes?.filter { it.wasHit }?.size ?: 0
    }
}