package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.nativeLibraries.NativeList


expect open class Quest {
    var id: String?
    var key: String
    var active: Boolean
    var leader: String?
    var RSVPNeeded: Boolean

    var members: NativeList<QuestMember>?
    var progress: QuestProgress?

    var participants: NativeList<Member>?
    var rageStrikes: NativeList<QuestRageStrike>?

    fun hasRageStrikes(): Boolean

    fun addRageStrike(rageStrike: QuestRageStrike)

    val activeRageStrikeNumber: Int
}