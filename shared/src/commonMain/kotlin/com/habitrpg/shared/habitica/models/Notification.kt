package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.models.notifications.AchievementData
import com.habitrpg.shared.habitica.models.notifications.ChallengeWonData
import com.habitrpg.shared.habitica.models.notifications.FirstDropData
import com.habitrpg.shared.habitica.models.notifications.GroupTaskApprovedData
import com.habitrpg.shared.habitica.models.notifications.GroupTaskNeedsWorkData
import com.habitrpg.shared.habitica.models.notifications.GroupTaskRequiresApprovalData
import com.habitrpg.shared.habitica.models.notifications.GuildInvitationData
import com.habitrpg.shared.habitica.models.notifications.ItemReceivedData
import com.habitrpg.shared.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.shared.habitica.models.notifications.NewChatMessageData
import com.habitrpg.shared.habitica.models.notifications.NewStuffData
import com.habitrpg.shared.habitica.models.notifications.NotificationData
import com.habitrpg.shared.habitica.models.notifications.PartyInvitationData
import com.habitrpg.shared.habitica.models.notifications.QuestInvitationData
import com.habitrpg.shared.habitica.models.notifications.UnallocatedPointsData
import com.habitrpg.shared.habitica.serialization.NotificationSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = NotificationSerializer::class)
class Notification {
    enum class Type(
        val type: String,
    ) {
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
        ITEM_RECEIVED("ITEM_RECEIVED"),

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
        ACHIEVEMENT_PET_COLOR("ACHIEVEMENT_PET_COLOR"),
        ACHIEVEMENT_ONBOARDING_COMPLETE("ONBOARDING_COMPLETE"),
        ACHIEVEMENT_ULTIMATE_GEAR("ULTIMATE_GEAR_ACHIEVEMENT"),

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

        REBIRTH_ENABLED("REBIRTH_ENABLED"),
        REBIRTH_ACHIEVEMENT("REBIRTH_ACHIEVEMENT"),

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

    fun getDataSerializer(): KSerializer<out NotificationData>? =
        when (type) {
            Type.LOGIN_INCENTIVE.type -> LoginIncentiveData.serializer()
            Type.NEW_STUFF.type -> NewStuffData.serializer()
            Type.NEW_CHAT_MESSAGE.type -> NewChatMessageData.serializer()
            Type.GROUP_TASK_NEEDS_WORK.type -> GroupTaskNeedsWorkData.serializer()
            Type.GROUP_TASK_APPROVED.type -> GroupTaskApprovedData.serializer()
            Type.GROUP_TASK_REQUIRES_APPROVAL.type -> GroupTaskRequiresApprovalData.serializer()
            Type.UNALLOCATED_STATS_POINTS.type -> UnallocatedPointsData.serializer()
            Type.GUILD_INVITATION.type -> GuildInvitationData.serializer()
            Type.PARTY_INVITATION.type -> PartyInvitationData.serializer()
            Type.QUEST_INVITATION.type -> QuestInvitationData.serializer()
            Type.FIRST_DROP.type -> FirstDropData.serializer()
            Type.ACHIEVEMENT_GENERIC.type -> AchievementData.serializer()
            Type.WON_CHALLENGE.type -> ChallengeWonData.serializer()
            Type.ITEM_RECEIVED.type -> ItemReceivedData.serializer()
            Type.ACHIEVEMENT_ALL_YOUR_BASE.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_BACK_TO_BASICS.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_JUST_ADD_WATER.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_LOST_MASTERCLASSER.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_MIND_OVER_MATTER.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_DUST_DEVIL.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_ARID_AUTHORITY.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_MONSTER_MAGUS.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_UNDEAD_UNDERTAKER.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_PRIMED_FOR_PAINTING.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_PEARLY_PRO.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_TICKLED_PINK.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_ROSY_OUTLOOK.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_BUG_BONANZA.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_BARE_NECESSITIES.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_FRESHWATER_FRIENDS.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_ALL_THAT_GLITTERS.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_BONE_COLLECTOR.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_SKELETON_CREW.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_SEEING_RED.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_RED_LETTER_DAY.type -> AchievementData.serializer()
            Type.ACHIEVEMENT_ULTIMATE_GEAR.type -> AchievementData.serializer()
            Type.REBIRTH_ENABLED.type -> AchievementData.serializer()
            Type.REBIRTH_ACHIEVEMENT.type -> AchievementData.serializer()

            else -> if (type?.contains("ACHIEVEMENT") == true) AchievementData.serializer() else null
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
