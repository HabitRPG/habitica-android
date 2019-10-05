package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList


expect open class Quest {
    var id: String?
    var key: String
    var active: Boolean
    var leader: String?
    var RSVPNeeded: Boolean

    var members: NativeRealmList<QuestMember>?
    var progress: QuestProgress?

    var participants: NativeRealmList<Member>?
    var rageStrikes: NativeRealmList<QuestRageStrike>?

    fun hasRageStrikes(): Boolean

    fun addRageStrike(rageStrike: QuestRageStrike)

    val activeRageStrikeNumber: Int
}