package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Quest : NativeRealmObject() {
    @PrimaryKeyAnnotation
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

    var members: NativeList<QuestMember>? = null
    var progress: QuestProgress? = null

    var participants: NativeList<Member>? = null
    var rageStrikes: NativeList<QuestRageStrike>? = null

    fun hasRageStrikes(): Boolean {
        return rageStrikes?.isNotEmpty() ?: false
    }

    fun addRageStrike(rageStrike: QuestRageStrike) {
        if (rageStrikes == null) {
            rageStrikes = NativeList()
        }
        rageStrikes?.add(rageStrike)
    }

    val activeRageStrikeNumber: Int
    get() {
        return rageStrikes?.filter { it.wasHit }?.size ?: 0
    }
}