package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper


expect open class Quest {
    var id: String?
    var key: String
    var active: Boolean
    var leader: String?
    var RSVPNeeded: Boolean

    var members: RealmListWrapper<QuestMember>?
    var progress: QuestProgress?

    var participants: RealmListWrapper<Member>?
    var rageStrikes: RealmListWrapper<QuestRageStrike>?

    fun hasRageStrikes(): Boolean

    fun addRageStrike(rageStrike: QuestRageStrike)

    val activeRageStrikeNumber: Int
}