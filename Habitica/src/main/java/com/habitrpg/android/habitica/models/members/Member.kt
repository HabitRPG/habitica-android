package com.habitrpg.android.habitica.models.members

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.*

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Member : RealmObject(), Avatar, BaseObject {

    @PrimaryKey
    @SerializedName("_id")
    var id: String? = null
    override var stats: Stats? = null
    var inbox: Inbox? = null
    override var preferences: MemberPreferences? = null
    override val gemCount: Int
        get() = 0
    override val hourglassCount: Int
        get() = 0
    var profile: Profile? = null
    var party: UserParty? = null
    var contributor: ContributorInfo? = null
    var backer: Backer? = null
    var authentication: Authentication? = null
    var items: Items? = null
    override var costume: Outfit? = null
    override var equipped: Outfit? = null

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

    override fun hasClass(): Boolean {
        return preferences?.disableClasses == false && stats?.habitClass?.isNotEmpty() == true
    }

    override val sleep: Boolean
        get() = preferences?.sleep ?: false
}
