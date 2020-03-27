package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.models.Avatar
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.user.*
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class Member : NativeRealmObject(), Avatar {
    @PrimaryKeyAnnotation
    @SerializedNameAnnotation("_id")
    var id: String? = null
        set(id) {
            field = id
            val subID = "m$id" // Do this to prevent the member object from overwriting the user ones
            if (stats != null && stats?.isManaged() != true) {
                stats?.userId = subID
            }
            if (items != null && items?.isManaged() != true) {
                items?.userId = subID
            }
            if (this.inbox != null && this.inbox?.isManaged() != true) {
                this.inbox?.userId = subID
            }
            if (preferences != null && preferences?.isManaged() != true) {
                preferences?.userId = subID
            }
            if (this.profile != null && this.profile?.isManaged() != true) {
                this.profile?.userId = subID
            }
            if (this.contributor != null && this.contributor?.isManaged() != true) {
                this.contributor?.userId = subID
            }
            if (costume != null && costume?.isManaged() != true) {
                costume?.userId = subID + "costume"
            }
            if (equipped != null && equipped?.isManaged() != true) {
                equipped?.userId = subID + "equipped"
            }
            if (this.authentication != null && this.authentication?.isManaged() != true) {
                this.authentication?.userId = subID
            }
        }
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
    override var preferences: MemberPreferences? = null
        set(preferences) {
            field = preferences
            if (preferences != null && this.id != null && !preferences.isManaged()) {
                preferences.userId = this.id ?: ""
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
            if (items != null && this.id != null && !items.isManaged()) {
                items.userId = this.id
            }
        }
    override var costume: Outfit? = null
        set(costume) {
            field = costume
            if (costume != null && this.id != null) {
                costume.userId = this.id + "costume"
            }
        }

    override var equipped: Outfit? = null
        set(equipped) {
            field = equipped
            if (equipped != null && this.id != null) {
                equipped.userId = this.id + "equipped"
            }
        }

    override var currentMount: String? = null
    override var currentPet: String? = null

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


    override var gemCount: Int = 0
    override var hourglassCount: Int = 0

    override fun hasClass(): Boolean {
        return preferences?.disableClasses == false && stats?.habitClass?.isNotEmpty() == true
    }

    override val sleep: Boolean
        get() {
            return preferences?.sleep ?: false
        }
}
