package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Quest : RealmObject() {
    @PrimaryKey
    actual var id: String? = null
        set(value) {
            field = value
            progress?.id = value
        }
    actual var key: String = ""
        set(value) {
            field = value
            progress?.key = key
        }
    actual var active: Boolean = false
    actual var leader: String? = null
    actual var RSVPNeeded: Boolean = false

    actual var members: RealmListWrapper<QuestMember>? = null
    actual var progress: QuestProgress? = null

    actual var participants: RealmListWrapper<Member>? = null
    actual var rageStrikes: RealmListWrapper<QuestRageStrike>? = null

    actual fun hasRageStrikes(): Boolean {
        return rageStrikes?.isNotEmpty() ?: false
    }

    actual fun addRageStrike(rageStrike: QuestRageStrike) {
        if (rageStrikes == null) {
            rageStrikes = RealmListWrapper()
        }
        rageStrikes?.add(rageStrike)
    }

    actual val activeRageStrikeNumber: Int
        get() {
            return rageStrikes?.filter { it.wasHit }?.size ?: 0
        }
}