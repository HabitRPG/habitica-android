package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.habitrpg.shared.habitica.nativeLibraries.NativeList

expect open class User {

    var tasks: TaskList?

    var id: String?

    var versionNumber: Int

    var balance: Double
    var stats: Stats?
    var inbox: Inbox?
    var preferences: Preferences?
    var profile: Profile?
    var party: UserParty?
    var items: Items?
    var authentication: Authentication?
    var flags: Flags?
    var contributor: ContributorInfo?
    var invitations: Invitations?
    var tags: List<Tag>
    var questAchievements: List<QuestAchievement>
    var pushDevices: List<PushDevice>?
    var purchased: Purchases?
    var tasksOrder: TasksOrder?
    var challenges: NativeList<ChallengeMembership>?
    var abTests: NativeList<ABTest>?
    var lastCron: Date?
    var needsCron: Boolean
    var loginIncentives: Int
    var streakCount: Int
    val petsFoundCount: Int
    val mountsTamedCount: Int
    val contributorColor: Int
    val username: String?
    val formattedUsername: String?

    fun getPreferences(): Preferences?
    fun setPreferences(preferences: Preferences?)

    fun getStats(): Stats?
    fun setStats(stats: Stats?)

    fun getGemCount(): Int

    fun getHourglassCount(): Int

    fun getCostume(): Outfit?

    fun getEquipped(): Outfit?

    fun hasClass(): Boolean

    fun getCurrentMount(): String?

    fun getCurrentPet(): String?

    fun getSleep(): Boolean

    fun hasParty(): Boolean
}
