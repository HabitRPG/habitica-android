package com.habitrpg.common.habitica.models

import com.habitrpg.common.habitica.models.notifications.AchievementData
import com.habitrpg.common.habitica.models.notifications.ChallengeWonData
import com.habitrpg.common.habitica.models.notifications.FirstDropData
import com.habitrpg.common.habitica.models.notifications.GroupTaskApprovedData
import com.habitrpg.common.habitica.models.notifications.GroupTaskNeedsWorkData
import com.habitrpg.common.habitica.models.notifications.GroupTaskRequiresApprovalData
import com.habitrpg.common.habitica.models.notifications.GuildInvitationData
import com.habitrpg.common.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import com.habitrpg.common.habitica.models.notifications.NewStuffData
import com.habitrpg.common.habitica.models.notifications.NotificationData
import com.habitrpg.common.habitica.models.notifications.PartyInvitationData
import com.habitrpg.common.habitica.models.notifications.QuestInvitationData
import com.habitrpg.common.habitica.models.notifications.UnallocatedPointsData

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
        WON_CHALLENGE("WON_CHALLENGE"),

        // Achievements
        ACHIEVEMENT_PARTY_UP("ACHIEVEMENT_PARTY_UP"),
        ACHIEVEMENT_PARTY_ON("ACHIEVEMENT_PARTY_ON"),
        ACHIEVEMENT_BEAST_MASTER("ACHIEVEMENT_BEAST_MASTER"),
        ACHIEVEMENT_MOUNT_MASTER("ACHIEVEMENT_MOUNT_MASTER"),
        ACHIEVEMENT_TRIAD_BINGO("ACHIEVEMENT_TRIAD_BINGO"),
        ACHIEVEMENT_GUILD_JOINED("GUILD_JOINED_ACHIEVEMENT"),
        ACHIEVEMENT_CHALLENGE_JOINED("CHALLENGE_JOINED_ACHIEVEMENT"),
        ACHIEVEMENT_INVITED_FRIEND("INVITED_FRIEND_ACHIEVEMENT"),
        ACHIEVEMENT_GENERIC("ACHIEVEMENT"),
        ACHIEVEMENT_ONBOARDING_COMPLETE("ONBOARDING_COMPLETE"),

        ACHIEVEMENT_ALL_YOUR_BASE("ACHIEVEMENT_ALL_YOUR_BASE"),
        ACHIEVEMENT_BACK_TO_BASICS("ACHIEVEMENT_BACK_TO_BASICS"),
        ACHIEVEMENT_JUST_ADD_WATER("ACHIEVEMENT_JUST_ADD_WATER"),
        ACHIEVEMENT_LOST_MASTERCLASSER("ACHIEVEMENT_LOST_MASTERCLASSER"),
        ACHIEVEMENT_MIND_OVER_MATTER("ACHIEVEMENT_MIND_OVER_MATTER"),
        ACHIEVEMENT_DUST_DEVIL("ACHIEVEMENT_DUST_DEVIL"),
        ACHIEVEMENT_ARID_AUTHORITY("ACHIEVEMENT_ARID_AUTHORITY"),
        ACHIEVEMENT_MONSTER_MAGUS("ACHIEVEMENT_MONSTER_MAGUS"),
        ACHIEVEMENT_UNDEAD_UNDERTAKER("ACHIEVEMENT_UNDEAD_UNDERTAKER"),
        ACHIEVEMENT_PRIMED_FOR_PAINTING("ACHIEVEMENT_PRIMED_FOR_PAINTING"),
        ACHIEVEMENT_PEARLY_PRO("ACHIEVEMENT_PEARLY_PRO"),
        ACHIEVEMENT_TICKLED_PINK("ACHIEVEMENT_TICKLED_PINK"),
        ACHIEVEMENT_ROSY_OUTLOOK("ACHIEVEMENT_ROSY_OUTLOOK"),
        ACHIEVEMENT_BUG_BONANZA("ACHIEVEMENT_BUG_BONANZA"),
        ACHIEVEMENT_BARE_NECESSITIES("ACHIEVEMENT_BARE_NECESSITIES"),
        ACHIEVEMENT_FRESHWATER_FRIENDS("ACHIEVEMENT_FRESHWATER_FRIENDS"),
        ACHIEVEMENT_GOOD_AS_GOLD("ACHIEVEMENT_GOOD_AS_GOLD"),
        ACHIEVEMENT_ALL_THAT_GLITTERS("ACHIEVEMENT_ALL_THAT_GLITTERS"),
        ACHIEVEMENT_BONE_COLLECTOR("ACHIEVEMENT_BONE_COLLECTOR"),
        ACHIEVEMENT_SKELETON_CREW("ACHIEVEMENT_SKELETON_CREW"),
        ACHIEVEMENT_SEEING_RED("ACHIEVEMENT_SEEING_RED"),
        ACHIEVEMENT_RED_LETTER_DAY("ACHIEVEMENT_RED_LETTER_DAY"),

        FIRST_DROP("FIRST_DROPS"),

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
            Type.FIRST_DROP.type -> FirstDropData::class.java
            Type.ACHIEVEMENT_GENERIC.type -> AchievementData::class.java
            Type.WON_CHALLENGE.type -> ChallengeWonData::class.java

            Type.ACHIEVEMENT_ALL_YOUR_BASE.type -> AchievementData::class.java
            Type.ACHIEVEMENT_BACK_TO_BASICS.type -> AchievementData::class.java
            Type.ACHIEVEMENT_JUST_ADD_WATER.type -> AchievementData::class.java
            Type.ACHIEVEMENT_LOST_MASTERCLASSER.type -> AchievementData::class.java
            Type.ACHIEVEMENT_MIND_OVER_MATTER.type -> AchievementData::class.java
            Type.ACHIEVEMENT_DUST_DEVIL.type -> AchievementData::class.java
            Type.ACHIEVEMENT_ARID_AUTHORITY.type -> AchievementData::class.java
            Type.ACHIEVEMENT_MONSTER_MAGUS.type -> AchievementData::class.java
            Type.ACHIEVEMENT_UNDEAD_UNDERTAKER.type -> AchievementData::class.java
            Type.ACHIEVEMENT_PRIMED_FOR_PAINTING.type -> AchievementData::class.java
            Type.ACHIEVEMENT_PEARLY_PRO.type -> AchievementData::class.java
            Type.ACHIEVEMENT_TICKLED_PINK.type -> AchievementData::class.java
            Type.ACHIEVEMENT_ROSY_OUTLOOK.type -> AchievementData::class.java
            Type.ACHIEVEMENT_BUG_BONANZA.type -> AchievementData::class.java
            Type.ACHIEVEMENT_BARE_NECESSITIES.type -> AchievementData::class.java
            Type.ACHIEVEMENT_FRESHWATER_FRIENDS.type -> AchievementData::class.java
            Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> AchievementData::class.java
            Type.ACHIEVEMENT_ALL_THAT_GLITTERS.type -> AchievementData::class.java
            Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> AchievementData::class.java
            Type.ACHIEVEMENT_BONE_COLLECTOR.type -> AchievementData::class.java
            Type.ACHIEVEMENT_SKELETON_CREW.type -> AchievementData::class.java
            Type.ACHIEVEMENT_SEEING_RED.type -> AchievementData::class.java
            Type.ACHIEVEMENT_RED_LETTER_DAY.type -> AchievementData::class.java

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
