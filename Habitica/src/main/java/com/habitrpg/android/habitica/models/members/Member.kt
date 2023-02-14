package com.habitrpg.android.habitica.models.members

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.Assignable
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Backer
import com.habitrpg.android.habitica.models.user.ContributorInfo
import com.habitrpg.android.habitica.models.user.Inbox
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.shared.habitica.models.Avatar
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Member : RealmObject(), Avatar, BaseMainObject, Assignable {

    @PrimaryKey
    @SerializedName("_id")
    override var id: String? = null
    override var stats: Stats? = null
    var inbox: Inbox? = null
    override var preferences: MemberPreferences? = null
    override var flags: MemberFlags? = null
    override val gemCount: Int
        get() = 0
    override val hourglassCount: Int
        get() = 0
    var profile: Profile? = null
    var party: UserParty? = null
    var contributor: ContributorInfo? = null
    var backer: Backer? = null
    override var balance: Double = 0.0
    override var authentication: Authentication? = null
    override var items: Items? = null
    override var costume: Outfit? = null
    override var equipped: Outfit? = null

    override var currentMount: String? = null
    override var currentPet: String? = null

    var participatesInQuest: Boolean? = null
    var loginIncentives: Int = 0

    val displayName: String
        get() = this.profile?.name ?: ""

    override val identifiableName: String
        get() = username ?: ""

    override val avatar: Avatar
        get() = this

    val petsFoundCount: Int
        get() = this.items?.pets?.size ?: 0
    val mountsTamedCount: Int
        get() = this.items?.mounts?.size ?: 0

    override val realmClass: Class<out RealmModel>
        get() = Member::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"
}
