package com.habitrpg.shared.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.models.invitations.Invitations
import com.habitrpg.shared.habitica.models.social.ChallengeMembership
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.habitrpg.shared.habitica.models.tasks.TaskList
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

actual open class User : RealmObject(), Avatar, VersionedObject {

    @Ignore
    actual var tasks: TaskList? = null

    @PrimaryKey
    @SerializedName("_id")
    actual var id: String? = null
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
        }

    @SerializedName("_v")
    override var versionNumber: Int = 0

    actual var balance: Double = 0.toDouble()
    actual var stats: Stats? = null
        set(stats) {
            field = stats
            if (stats != null && this.id != null && !stats.isManaged) {
                stats.userId = this.id
            }
        }
    actual var inbox: Inbox? = null
        set(inbox) {
            field = inbox
            if (inbox != null && this.id != null && !inbox.isManaged) {
                inbox.userId = this.id
            }
        }
    actual var preferences: Preferences? = null
        set(preferences) {
            field = preferences
            if (preferences != null && this.id != null && !preferences.isManaged) {
                preferences.userId = this.id
            }
        }
    actual var profile: Profile? = null
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
    actual var items: Items? = null
        set(items) {
            field = items
            if (items != null && this.id != null && !items.isManaged) {
                items.userId = this.id
            }
        }
    @SerializedName("auth")
    actual var authentication: Authentication? = null
        set(authentication) {
            field = authentication
            if (authentication != null && this.id != null) {
                authentication.userId = this.id
            }
        }
    actual var flags: Flags? = null
        set(flags) {
            field = flags
            if (flags != null && this.id != null) {
                flags.userId = this.id
            }
        }
    actual var contributor: ContributorInfo? = null
        set(contributor) {
            field = contributor
            if (contributor != null && this.id != null && !contributor.isManaged) {
                contributor.userId = this.id
            }
        }
    actual var invitations: Invitations? = null
        set(invitations) {
            field = invitations
            if (invitations != null && this.id != null && !invitations.isManaged) {
                invitations.userId = this.id
            }
        }

    actual var tags = RealmList<Tag>()
    actual var questAchievements = RealmList<QuestAchievement>()
        set(value) {
            field = value
            field.forEach { it.userID = id }
        }

    @Ignore
    actual var pushDevices: List<PushDevice>? = null

    actual var purchased: Purchases? = null
        set(purchased) {
            field = purchased
            if (purchased != null && this.id != null) {
                purchased.userId = this.id
            }
        }

    @Ignore
    actual var tasksOrder: TasksOrder? = null

    actual var challenges: RealmList<ChallengeMembership>? = null

    actual var abTests: RealmList<ABTest>? = null

    actual var lastCron: Date? = null
    actual var needsCron: Boolean = false
    actual var loginIncentives: Int = 0
    actual var streakCount: Int = 0

    actual val petsFoundCount: Int
        get() = this.items?.pets?.size ?: 0

    actual val mountsTamedCount: Int
        get() = this.items?.mounts?.size ?: 0

    actual val contributorColor: Int
        get() = this.contributor?.contributorColor ?: android.R.color.black
    actual val username: String?
        get() = authentication?.localAuthentication?.username
    actual val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    override fun getGemCount(): Int {
        return (this.balance * 4).toInt()
    }

    override fun getHourglassCount(): Int {
        return purchased?.plan?.consecutive?.trinkets ?: 0
    }

    override fun getCostume(): Outfit? {
        return items?.gear?.costume
    }

    override fun getEquipped(): Outfit? {
        return items?.gear?.equipped
    }

    override fun hasClass(): Boolean {
        return preferences?.disableClasses != true && flags?.classSelected == true && stats?.habitClass?.isNotEmpty() == true
    }

    override fun getCurrentMount(): String? {
        return items?.currentMount ?: ""
    }

    override fun getCurrentPet(): String? {
        return items?.currentPet ?: ""
    }

    override fun getSleep(): Boolean {
        return preferences?.sleep ?: false
    }

    actual fun hasParty(): Boolean {
        return this.party?.id?.length ?: 0 > 0
    }
}
