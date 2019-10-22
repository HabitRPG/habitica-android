package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.Avatar
import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.user.*

actual open class Member : Avatar {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var stats: Stats?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var inbox: Inbox?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var preferences: MemberPreferences?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var profile: Profile?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var party: UserParty?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var contributor: ContributorInfo?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var authentication: Authentication?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var items: Items?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var currentMount: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var currentPet: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var sleep: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var gemCount: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var hourglassCount: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var costume: Outfit?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var equipped: Outfit?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual override var valid: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var participatesInQuest: Boolean?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var loginIncentives: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual val displayName: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val petsFoundCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val mountsTamedCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val username: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val formattedUsername: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual override fun hasClass(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}