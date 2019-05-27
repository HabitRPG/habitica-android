package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.PushDevice
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Tag
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

open class User : RealmObject(), Avatar {

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
        }
    var balance: Double = 0.toDouble()
    private var stats: Stats? = null
    var inbox: Inbox? = null
        set(inbox) {
            field = inbox
            if (inbox != null && this.id != null && !inbox.isManaged) {
                inbox.userId = this.id
            }
        }
    private var preferences: Preferences? = null
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
    var invitations: Invitations? = null
        set(invitations) {
            field = invitations
            if (invitations != null && this.id != null && !invitations.isManaged) {
                invitations.userId = this.id
            }
        }

    var tags = RealmList<Tag>()
    var questAchievements = RealmList<QuestAchievement>()
        set(value) {
            field = value
            field.forEach { it.userID = id }
        }

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
        get() = this.contributor?.contributorColor ?: android.R.color.black
    val username: String?
    get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    override fun getPreferences(): Preferences? {
        return preferences
    }

    fun setPreferences(preferences: Preferences?) {
        this.preferences = preferences
        if (preferences != null && this.id != null && !preferences.isManaged) {
            preferences.userId = this.id
        }
    }

    override fun getStats(): Stats? {
        return stats
    }

    fun setStats(stats: Stats?) {
        this.stats = stats
        if (stats != null && this.id != null && !stats.isManaged) {
            stats.userId = this.id
        }
    }

    override fun getGemCount(): Int {
        return (this.balance * 4).toInt()
    }

    override fun getHourglassCount(): Int? {
        return if (purchased != null) {
            purchased?.plan?.consecutive?.trinkets
        } else 0
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
        return getPreferences() != null && getPreferences()!!.sleep
    }

    fun hasParty(): Boolean {
        return this.party?.id?.length ?: 0 > 0
    }
}
