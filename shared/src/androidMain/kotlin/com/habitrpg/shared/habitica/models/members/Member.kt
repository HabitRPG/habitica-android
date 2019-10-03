package com.habitrpg.shared.habitica.models.members

import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.Avatar
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.user.*

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Member : RealmObject(), Avatar {


    @PrimaryKey
    @SerializedName("_id")
    actual var id: String? = null
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
                preferences?.userId = subID
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
    actual override var stats: Stats? = null
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
    actual override var preferences: MemberPreferences? = null
        set(preferences) {
            field = preferences
            if (field != null && this.id != null && field!!.isManaged) {
                field!!.userId = this.id ?: ""
            }
        }
    actual var profile: Profile? = null
        set(profile) {
            field = profile
            if (profile != null && this.id != null && !profile.isManaged) {
                profile.userId = this.id
            }
        }
    actual var party: UserParty? = null
        set(party) {
            field = party
            if (party != null && this.id != null && !party.isManaged) {
                party.userId = this.id
            }
        }
    actual var contributor: ContributorInfo? = null
        set(contributor) {
            field = contributor
            if (contributor != null && this.id != null && !contributor.isManaged) {
                contributor.userId = this.id
            }
        }
    actual var authentication: Authentication? = null
        set(authentication) {
            field = authentication
            if (authentication != null && this.id != null) {
                authentication.userId = this.id
            }
        }
    actual var items: Items? = null
        set(items) {
            field = items
            if (items != null && this.id != null && !items.isManaged) {
                items.userId = this.id
            }
        }

    actual override var currentMount: String? = null
    actual override var currentPet: String? = null
    actual override var sleep: Boolean = false
    actual override var gemCount: Int? = 0
        get() = 0
    actual override var hourglassCount: Int? = 0
        get() = 0

    actual override var costume: Outfit? = null
        set(costume) {
            field = costume
            if (costume != null && this.id != null) {
                costume.userId = this.id + "costume"
            }
        }
    actual override var equipped: Outfit? = null
        set(equipped) {
            field = equipped
            if (equipped != null && this.id != null) {
                equipped.userId = this.id + "equipped"
            }
        }
    actual override var valid: Boolean = false // TODO Tyler


    actual var participatesInQuest: Boolean? = null
    actual var loginIncentives: Int = 0

    actual val displayName: String
        get() = if (this.profile == null) {
            ""
        } else this.profile?.name ?: ""

    actual val petsFoundCount: Int
        get() = this.items?.pets?.size ?: 0
    actual val mountsTamedCount: Int
        get() = this.items?.mounts?.size ?: 0

    actual val username: String?
        get() = authentication?.localAuthentication?.username
    actual val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    actual override fun hasClass(): Boolean {
        return preferences?.isDisableClasses == false && stats?.habitClass?.isNotEmpty() == true
    }

}

