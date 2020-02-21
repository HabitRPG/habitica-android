package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.*
import com.habitrpg.shared.habitica.models.invitations.Invitations
import com.habitrpg.shared.habitica.models.social.ChallengeMembership
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.tasks.TaskList
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
        }

    @SerializedNameAnnotation("_v")
    override var versionNumber: Int = 0

    var balance: Double = 0.toDouble()
    override var stats: Stats? = null
    var inbox: Inbox? = null
        set(inbox) {
            field = inbox
            if (inbox != null && this.id != null && !inbox.isManaged()) {
                inbox.userId = this.id
            }
        }
    override var preferences: Preferences? = null
    get(): Preferences? {
        return field
    }
    set(preferences: Preferences?) {
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
        get() = this.contributor?.contributorColor ?: NativeColour.black
    val username: String?
    get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null


    // TODO multi check that missing override is acceptable
    fun getStats(): Stats? {
        return stats
    }

    fun setStats(stats: Stats?) {
        this.stats = stats
        if (stats != null && this.id != null && !stats.isManaged()) {
            stats.userId = this.id
        }
    }

    override val gemCount: Int?
    get() {
        return (this.balance * 4).toInt()
    }

    override val hourglassCount: Int?
    get() {
        return purchased?.plan?.consecutive?.trinkets ?: 0
    }

    override val costume: Outfit?
    get() {
        return items?.gear?.costume
    }

    override val equipped: Outfit?
    get() {
        return items?.gear?.equipped
    }

    override fun hasClass(): Boolean {
        return preferences?.disableClasses != true && flags?.classSelected == true && stats?.habitClass?.isNotEmpty() == true
    }

    override val currentMount: String?
        get() {
        return items?.currentMount ?: ""
    }

    override val currentPet: String?
        get() {
        return items?.currentPet ?: ""
    }

    override var sleep: Boolean? = null
    fun getSleep(): Boolean {
        return preferences?.sleep ?: false
    }

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

    override fun isValid(): Boolean {
        return true;
    }
}
