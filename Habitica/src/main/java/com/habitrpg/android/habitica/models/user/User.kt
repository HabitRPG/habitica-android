package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.invitations.Invitations
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

open class User : RealmObject(), BaseObject, Avatar, VersionedObject {

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
        set(id) {
            field = id
            if (stats?.isManaged != true) {
                stats?.userId = id
            }
            if (inbox?.isManaged != true) {
                this.inbox?.userId = id
            }
            if (preferences?.isManaged != true) {
                preferences?.userId = id
            }
            if (this.profile?.isManaged != true) {
                this.profile?.userId = id
            }
            if (this.items?.isManaged != true) {
                this.items?.userId = id
            }
            if (this.authentication?.isManaged != true) {
                this.authentication?.userId = id
            }
            if (this.flags?.isManaged != true) {
                this.flags?.userId = id
            }
            if (this.contributor?.isManaged != true) {
                this.contributor?.userId = id
            }
            if (this.invitations?.isManaged != true) {
                this.invitations?.userId = id
            }
            for (test in abTests ?: emptyList<ABTest>()) {
                test.userID = id
            }
            for (achievement in achievements) {
                achievement.userId = id
            }
        }

    @SerializedName("_v")
    override var versionNumber: Int = 0

    var balance: Double = 0.toDouble()
    override var stats: Stats? = null
        set(value) {
            field = value
            if (value != null && this.id != null && !value.isManaged) {
                field?.userId = this.id
            }
        }
    var inbox: Inbox? = null
        set(inbox) {
            field = inbox
            if (inbox != null && this.id != null && !inbox.isManaged) {
                inbox.userId = this.id
            }
        }
    override var preferences: Preferences? = null
    set(value) {
        field = value
        if (value != null && this.id != null && !value.isManaged) {
            field?.userId = this.id
        }
    }
    var profile: Profile? = null
        set(profile) {
            field = profile
            if (profile != null && this.id != null && !profile.isManaged) {
                profile.userId = this.id
            }
        }
    var party: UserParty? = null
        set(party) {
            field = party
            if (party != null && this.id != null && !party.isManaged) {
                party.userId = this.id
            }
        }
    var items: Items? = null
        set(items) {
            field = items
            if (items != null && this.id != null && !items.isManaged) {
                items.userId = this.id
            }
        }
    @SerializedName("auth")
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
            if (contributor != null && this.id != null && !contributor.isManaged) {
                contributor.userId = this.id
            }
        }
    var backer: Backer? = null
        set(backer) {
            field = backer
            if (backer != null && this.id != null && !backer.isManaged) {
                backer.id = this.id
            }
        }
    var invitations: Invitations? = null
        set(invitations) {
            field = invitations
            if (invitations != null && this.id != null && !invitations.isManaged) {
                invitations.userId = this.id
            }
        }

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
        set(purchased) {
            field = purchased
            if (purchased != null && this.id != null) {
                purchased.userId = this.id
            }
        }

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
