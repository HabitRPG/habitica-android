package com.habitrpg.android.habitica.models.members

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.user.ContributorInfo
import com.habitrpg.shared.habitica.models.user.Profile

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Member : RealmObject(), Avatar {


    @PrimaryKey
    @SerializedName("_id")
    var id: String? = null
        set(id) {
            field = id
            val subID = "m$id" // Do this to prevent the member object from overwriting the user ones
            if (stats != null && stats?.isManaged != true) {
                stats?.userId = subID
            }
            if (items != null && items?.isManaged != true) {
                items?.userId = subID
            }
            if (this.inbox != null && this.inbox?.isManaged != true) {
                this.inbox?.userId = subID
            }
            if (preferences != null && preferences?.isManaged != true) {
                preferences?.setUserId(subID)
            }
            if (this.profile != null && this.profile?.isManaged != true) {
                this.profile?.userId = subID
            }
            if (this.contributor != null && this.contributor?.isManaged != true) {
                this.contributor?.userId = subID
            }
            if (costume != null && costume?.isManaged != true) {
                costume?.userId = subID + "costume"
            }
            if (equipped != null && equipped?.isManaged != true) {
                equipped?.userId = subID + "equipped"
            }
            if (this.authentication != null && this.authentication?.isManaged != true) {
                this.authentication?.userId = subID
            }
        }
    private var stats: Stats? = null
    var inbox: Inbox? = null
        set(inbox) {
            field = inbox
            if (inbox != null && this.id != null && !inbox.isManaged) {
                inbox.userId = this.id
            }
        }
    private var preferences: MemberPreferences? = null
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
    var contributor: ContributorInfo? = null
        set(contributor) {
            field = contributor
            if (contributor != null && this.id != null && !contributor.isManaged) {
                contributor.userId = this.id
            }
        }
    var authentication: Authentication? = null
        set(authentication) {
            field = authentication
            if (authentication != null && this.id != null) {
                authentication.userId = this.id
            }
        }
    var items: Items? = null
        set(items) {
            field = items
            if (items != null && this.id != null && !items.isManaged) {
                items.userId = this.id
            }
        }
    private var costume: Outfit? = null
    private var equipped: Outfit? = null

    private var currentMount: String? = null
    private var currentPet: String? = null

    var participatesInQuest: Boolean? = null
    var loginIncentives: Int = 0

    val displayName: String
        get() = if (this.profile == null) {
            ""
        } else this.profile?.name ?: ""

    val petsFoundCount: Int
        get() = this.items?.pets?.size ?: 0
    val mountsTamedCount: Int
        get() = this.items?.mounts?.size ?: 0

    val username: String?
    get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    override fun getPreferences(): MemberPreferences? {
        return preferences
    }

    fun setPreferences(preferences: MemberPreferences?) {
        this.preferences = preferences
        if (preferences != null && this.id != null && !preferences.isManaged) {
            preferences.setUserId(this.id ?: "")
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

    override fun getGemCount(): Int? {
        return 0
    }

    override fun getHourglassCount(): Int? {
        return 0
    }

    override fun getCostume(): Outfit? {
        return costume
    }

    fun setCostume(costume: Outfit?) {
        this.costume = costume
        if (costume != null && this.id != null) {
            costume.userId = this.id + "costume"
        }
    }

    override fun getEquipped(): Outfit? {
        return equipped
    }

    override fun hasClass(): Boolean {
        return preferences?.disableClasses == false && stats?.habitClass?.isNotEmpty() == true
    }

    fun setEquipped(equipped: Outfit?) {
        this.equipped = equipped
        if (equipped != null && this.id != null) {
            equipped.userId = this.id + "equipped"
        }
    }

    override fun getCurrentMount(): String? {
        return currentMount
    }

    fun setCurrentMount(currentMount: String) {
        this.currentMount = currentMount
    }

    override fun getCurrentPet(): String? {
        return currentPet
    }

    fun setCurrentPet(currentPet: String) {
        this.currentPet = currentPet
    }

    override fun getSleep(): Boolean {
        return getPreferences()?.sleep ?: false
    }
}
