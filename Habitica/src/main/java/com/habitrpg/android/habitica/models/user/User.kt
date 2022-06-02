package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.PushDevice
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.VersionedObject
import com.habitrpg.android.habitica.models.invitations.Invitations
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.common.habitica.models.tasks.TasksOrder
import com.habitrpg.common.habitica.models.Avatar
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.Date

open class User : RealmObject(), BaseMainObject, Avatar, VersionedObject {

    override val realmClass: Class<User>
        get() = User::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    @Ignore
    var tasks: TaskList? = null

    @PrimaryKey
    @SerializedName("_id")
    var id: String? = null

    @SerializedName("_v")
    override var versionNumber: Int = 0

    var balance: Double = 0.toDouble()
    override var stats: Stats? = null
    var inbox: Inbox? = null
    override var preferences: Preferences? = null
    var profile: Profile? = null
    var party: UserParty? = null
    var items: Items? = null
    @SerializedName("auth")
    var authentication: Authentication? = null
    override var flags: Flags? = null
    var contributor: ContributorInfo? = null
    var backer: Backer? = null
    var invitations: Invitations? = null

    var tags = RealmList<Tag>()
    var achievements = RealmList<UserAchievement>()
    var questAchievements = RealmList<QuestAchievement>()
        set(value) {
            field = value
            field.forEach { it.userID = id }
        }
    var challengeAchievements = RealmList<String>()

    @Ignore
    var pushDevices: List<PushDevice>? = null

    var purchased: Purchases? = null

    @Ignore
    var tasksOrder: TasksOrder? = null

    var challenges: RealmList<ChallengeMembership>? = null

    var abTests: RealmList<ABTest>? = null

    var lastCron: Date? = null
    var needsCron: Boolean = false
    var loginIncentives: Int = 0
    var streakCount: Int = 0

    val petsFoundCount: Int
        get() = this.items?.pets?.size ?: 0

    val mountsTamedCount: Int
        get() = this.items?.mounts?.size ?: 0

    val contributorColor: Int
        get() = this.contributor?.contributorColor ?: R.color.text_primary
    val username: String?
        get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    override val gemCount: Int
        get() = (this.balance * 4).toInt()

    override val hourglassCount: Int
        get() = purchased?.plan?.consecutive?.trinkets ?: 0

    override val costume: Outfit?
        get() = items?.gear?.costume

    override val equipped: Outfit?
        get() = items?.gear?.equipped

    override val hasClass: Boolean
        get() {
            return preferences?.disableClasses != true && flags?.classSelected == true && stats?.habitClass?.isNotEmpty() == true
        }

    override val currentMount: String?
        get() = items?.currentMount ?: ""
    override val currentPet: String?
        get() = items?.currentPet ?: ""

    override val sleep: Boolean
        get() = preferences?.sleep ?: false

    val hasParty: Boolean
        get() {
            return this.party?.id?.length ?: 0 > 0
        }

    val isSubscribed: Boolean
        get() {
            return purchased?.plan?.isActive == true
        }

    val onboardingAchievements: List<UserAchievement>
        get() {
            val onboarding = mutableMapOf<String, UserAchievement>()
            for (key in ONBOARDING_ACHIEVEMENT_KEYS) {
                val achievement = UserAchievement()
                achievement.key = key
                onboarding[key] = achievement
            }
            for (achievement in achievements) {
                if (achievement.key in ONBOARDING_ACHIEVEMENT_KEYS) {
                    onboarding[achievement.key ?: ""] = achievement
                }
            }
            return onboarding.values.toList()
        }

    val hasCompletedOnboarding: Boolean
        get() {
            val onboarding = onboardingAchievements
            return onboarding.count { it.earned } == onboarding.size
        }

    companion object {
        val ONBOARDING_ACHIEVEMENT_KEYS = listOf("createdTask", "completedTask", "hatchedPet", "fedPet", "purchasedEquipment")
    }
}
