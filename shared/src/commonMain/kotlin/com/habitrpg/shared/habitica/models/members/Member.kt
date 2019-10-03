package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.Avatar
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.user.*


expect open class Member : Avatar {

    var id: String?
    override var stats: Stats?
    var inbox: Inbox?
    override var preferences: MemberPreferences?
    var profile: Profile?
    var party: UserParty? 
    var contributor: ContributorInfo? 
    var authentication: Authentication? 
    var items: Items?

    override var currentMount: String?
    override var currentPet: String?
    override var sleep: Boolean
    override var gemCount: Int?
    override var hourglassCount: Int?

    override var costume: Outfit?
    override var equipped: Outfit?
    override var valid: Boolean

    var participatesInQuest: Boolean?
    var loginIncentives: Int

    val displayName: String

    val petsFoundCount: Int
    val mountsTamedCount: Int

    val username: String?
    val formattedUsername: String?

    override fun hasClass(): Boolean
}
