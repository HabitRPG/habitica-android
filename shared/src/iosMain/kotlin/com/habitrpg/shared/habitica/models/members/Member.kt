package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.models.social.UserParty
import com.habitrpg.shared.habitica.models.user.*

actual open class Member : Avatar {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual final override var stats: Stats?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var inbox: Inbox?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual final override var preferences: MemberPreferences?
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
    actual final override var costume: Outfit?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual final override var equipped: Outfit?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual final override var currentMount: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual final override var currentPet: String?
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

    actual open fun getPreferences(): MemberPreferences? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setPreferences(preferences: MemberPreferences?) {
    }

    actual open fun getStats(): Stats? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setStats(stats: Stats?) {
    }

    actual open fun getGemCount(): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual open fun getHourglassCount(): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual open fun getCostume(): Outfit? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setCostume(costume: Outfit?) {
    }

    actual open fun getEquipped(): Outfit? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun hasClass(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setEquipped(equipped: Outfit?) {
    }

    actual open fun getCurrentMount(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setCurrentMount(currentMount: String) {
    }

    actual open fun getCurrentPet(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setCurrentPet(currentPet: String) {
    }

    actual open fun getSleep(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}