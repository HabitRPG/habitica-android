package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.Avatar
import com.habitrpg.shared.habitica.models.PushDevice
import com.habitrpg.shared.habitica.models.QuestAchievement
import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.models.invitations.Invitations
import com.habitrpg.shared.habitica.models.social.ChallengeMembership
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList

expect open class User: Avatar {

    var tasks: TaskList?

    var id: String?

    var versionNumber: Int

    var balance: Double
    override var stats: Stats?
    var inbox: Inbox?
    override var preferences: Preferences?
    var profile: Profile?
    var party: UserParty?
    var items: Items?
    var authentication: Authentication?
    var flags: Flags?
    var contributor: ContributorInfo?
    var invitations: Invitations?
    var tags: NativeRealmList<Tag>
    var questAchievements: NativeRealmList<QuestAchievement>
    var pushDevices: List<PushDevice>?
    var purchased: Purchases?
    var tasksOrder: TasksOrder?
    var challenges: NativeRealmList<ChallengeMembership>?
    var abTests: NativeRealmList<ABTest>?
    var lastCron: NativeDate?
    var needsCron: Boolean
    var loginIncentives: Int
    var streakCount: Int
    val petsFoundCount: Int
    val mountsTamedCount: Int
    val contributorColor: Int
    val username: String?
    val formattedUsername: String?

    override fun hasClass(): Boolean

    fun hasParty(): Boolean
}
