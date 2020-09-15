package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.*
import com.habitrpg.shared.habitica.models.invitations.Invitations
import com.habitrpg.shared.habitica.models.social.ChallengeMembership
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.habitrpg.shared.habitica.nativePackages.NativeColor
import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.IgnoreAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class User : NativeRealmObject(), Avatar, VersionedObject {

    @IgnoreAnnotation
    var tasks: TaskList? = null

    @PrimaryKeyAnnotation
    @SerializedNameAnnotation("_id")
    var id: String? = null
        set(id) {
            field = id
            if (stats?.isManaged() != true) {
                stats?.userId = id
            }
            if (inbox?.isManaged() != true) {
                this.inbox?.userId = id
            }
            if (preferences?.isManaged() != true) {
                preferences?.userId = id
            }
            if (this.profile?.isManaged() != true) {
                this.profile?.userId = id
            }
            if (this.items?.isManaged() != true) {
                this.items?.userId = id
            }
            if (this.authentication?.isManaged() != true) {
                this.authentication?.userId = id
            }
            if (this.flags?.isManaged() != true) {
                this.flags?.userId = id
            }
            if (this.contributor?.isManaged() != true) {
                this.contributor?.userId = id
            }
            if (this.invitations?.isManaged() != true) {
                this.invitations?.userId = id
            }
            for (test in abTests ?: emptyList<ABTest>()) {
                test.userID = id
            }
            for (achievement in achievements) {
                achievement.userId = id
            }
        }

    @SerializedNameAnnotation("_v")
    override var versionNumber: Int = 0

    var balance: Double = 0.toDouble()
    override var stats: Stats? = null
        set(stats) {
            field = stats
            if (stats != null && this.id != null && !stats.isManaged()) {
                stats.userId = this.id
            }
        }

    var inbox: Inbox? = null
        set(inbox) {
            field = inbox
            if (inbox != null && this.id != null && !inbox.isManaged()) {
                inbox.userId = this.id
            }
        }
    override var preferences: Preferences? = null
        set(preferences) {
            field = preferences
            if (preferences != null && this.id != null && !preferences.isManaged()) {
                preferences.userId = this.id
            }
        }

    var profile: Profile? = null
        set(profile) {
            field = profile
            if (profile != null && this.id != null && !profile.isManaged()) {
                profile.userId = this.id
            }
        }
    var party: UserParty? = null
        set(party) {
            field = party
            if (party != null && this.id != null && !party.isManaged()) {
                party.userId = this.id
            }
        }
    var items: Items? = null
        set(items) {
            field = items
            if (items != null && this.id != null && !items.isManaged()) {
                items.userId = this.id
            }
        }
    @SerializedNameAnnotation("auth")
    var authentication: Authentication? = null
        set(authentication) {
            field = authentication
            if (authentication != null && this.id != null) {
                authentication.userId = this.id
            }
        }
    var flags: Flags? = null
        set(flags) {
            field = flags
            if (flags != null && this.id != null) {
                flags.userId = this.id
            }
        }
    var contributor: ContributorInfo? = null
        set(contributor) {
            field = contributor
            if (contributor != null && this.id != null && !contributor.isManaged()) {
                contributor.userId = this.id
            }
        }
    var backer: Backer? = null
        set(backer) {
            field = backer
            if (backer != null && this.id != null && !backer.isManaged()) {
                backer.id = this.id
            }
        }
    var invitations: Invitations? = null
        set(invitations) {
            field = invitations
            if (invitations != null && this.id != null && !invitations.isManaged()) {
                invitations.userId = this.id
            }
        }

    var tags = NativeList<Tag>()
    var achievements = NativeList<UserAchievement>()
    var questAchievements = NativeList<QuestAchievement>()
        set(value) {
            field = value
            field.forEach { it.userID = id }
        }

    @IgnoreAnnotation
    var pushDevices: List<PushDevice>? = null

    var purchased: Purchases? = null
        set(purchased) {
            field = purchased
            if (purchased != null && this.id != null) {
                purchased.userId = this.id
            }
        }

    @IgnoreAnnotation
    var tasksOrder: TasksOrder? = null

    var challenges: NativeList<ChallengeMembership>? = null

    var abTests: NativeList<ABTest>? = null

    var lastCron: NativeDate? = null
    var needsCron: Boolean = false
    var loginIncentives: Int = 0
    var streakCount: Int = 0

    val petsFoundCount: Int
        get() = this.items?.pets?.size ?: 0

    val mountsTamedCount: Int
        get() = this.items?.mounts?.size ?: 0

    val contributorColor: Int
        get() = this.contributor?.contributorColor ?: NativeColor.black
    val username: String?
        get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    override val gemCount: Int?
        get() = (this.balance * 4).toInt()

    override val hourglassCount: Int?
        get() = purchased?.plan?.consecutive?.trinkets ?: 0

    override val costume: Outfit?
        get() = items?.gear?.costume

    override val equipped: Outfit?
        get() = items?.gear?.equipped

    override fun hasClass(): Boolean {
        return preferences?.disableClasses != true && flags?.classSelected == true && stats?.habitClass?.isNotEmpty() == true
    }

    override val currentMount: String?
        get() = items?.currentMount ?: ""

    override val currentPet: String?
        get() = items?.currentPet ?: ""

    override val sleep: Boolean
        get() = preferences?.sleep ?: false

    fun hasParty(): Boolean {
        return this.party?.id?.length ?: 0 > 0
    }

    val isSubscribed: Boolean
        get() {
            val plan = purchased?.plan
            var isSubscribed = false
            if (plan != null) {
                if (plan.isActive) {
                    isSubscribed = true
                }
            }
            return isSubscribed
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
