package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.notifications.*

class Notification {
    enum class Type(val type: String) {
        // Notification types coming from the server
        LOGIN_INCENTIVE("LOGIN_INCENTIVE"),
        NEW_STUFF("NEW_STUFF"),
        NEW_CHAT_MESSAGE("NEW_CHAT_MESSAGE"),
        NEW_MYSTERY_ITEMS("NEW_MYSTERY_ITEMS"),
        GROUP_TASK_NEEDS_WORK("GROUP_TASK_NEEDS_WORK"),
        GROUP_TASK_APPROVED("GROUP_TASK_APPROVED"),
        GROUP_TASK_REQUIRES_APPROVAL("GROUP_TASK_REQUIRES_APPROVAL"),
        UNALLOCATED_STATS_POINTS("UNALLOCATED_STATS_POINTS"),

        //Achievements
        ACHIEVEMENT_PARTY_UP("ACHIEVEMENT_PARTY_UP"),
        ACHIEVEMENT_PARTY_ON("ACHIEVEMENT_PARTY_ON"),
        ACHIEVEMENT_BEAST_MASTER("ACHIEVEMENT_BEAST_MASTER"),
        ACHIEVEMENT_MOUNT_MASTER("ACHIEVEMENT_MOUNT_MASTER"),
        ACHIEVEMENT_TRIAD_BINGO("ACHIEVEMENT_TRIAD_BINGO"),
        ACHIEVEMENT_GUILD_JOINED("GUILD_JOINED_ACHIEVEMENT"),
        ACHIEVEMENT_CHALLENGE_JOINED("CHALLENGE_JOINED_ACHIEVEMENT"),
        ACHIEVEMENT_INVITED_FRIEND("INVITED_FRIEND_ACHIEVEMENT"),

        // Custom notification types (created by this app)
        GUILD_INVITATION("GUILD_INVITATION"),
        PARTY_INVITATION("PARTY_INVITATION"),
        QUEST_INVITATION("QUEST_INVITATION"),
    }

    var id: String = ""

    var type: String? = null
    var seen: Boolean? = null

    var data: NotificationData? = null

    fun getDataType(): java.lang.reflect.Type? {
        return when (type) {
            Type.LOGIN_INCENTIVE.type -> LoginIncentiveData::class.java
            Type.NEW_STUFF.type -> NewStuffData::class.java
            Type.NEW_CHAT_MESSAGE.type -> NewChatMessageData::class.java
            Type.GROUP_TASK_NEEDS_WORK.type -> GroupTaskNeedsWorkData::class.java
            Type.GROUP_TASK_APPROVED.type -> GroupTaskApprovedData::class.java
            Type.GROUP_TASK_REQUIRES_APPROVAL.type -> GroupTaskRequiresApprovalData::class.java
            Type.UNALLOCATED_STATS_POINTS.type -> UnallocatedPointsData::class.java
            Type.GUILD_INVITATION.type -> GuildInvitationData::class.java
            Type.PARTY_INVITATION.type -> PartyInvitationData::class.java
            Type.QUEST_INVITATION.type -> QuestInvitationData::class.java
            else -> null
        }
    }

    val priority: Int
    get() {
        return when (type) {
            Type.NEW_STUFF.type -> 1
            Type.GUILD_INVITATION.type -> 2
            Type.PARTY_INVITATION.type -> 3
            Type.UNALLOCATED_STATS_POINTS.type -> 4
            Type.NEW_MYSTERY_ITEMS.type -> 5
            Type.NEW_CHAT_MESSAGE.type -> 6
            else -> 100
        }
    }
}
