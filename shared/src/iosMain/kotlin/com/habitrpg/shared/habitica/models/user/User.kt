package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.Tag


actual open class User {
    actual var tasks: TaskList?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var versionNumber: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var balance: Double
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var stats: Stats?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var inbox: Inbox?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var preferences: Preferences?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var profile: Profile?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var party: UserParty?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var items: Items?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var authentication: Authentication?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var flags: Flags?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var contributor: ContributorInfo?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var invitations: Invitations?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var tags: List<Tag>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var questAchievements: List<QuestAchievement>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var pushDevices: List<PushDevice>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var purchased: Purchases?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var tasksOrder: TasksOrder?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var challenges: RealmList<ChallengeMembership><ChallengeMembership>?
    actual var abTests: RealmList<ABTest><ABTest>?
    actual var lastCron: Date?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var needsCron: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var loginIncentives: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var streakCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual val petsFoundCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val mountsTamedCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val contributorColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val username: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val formattedUsername: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun getPreferences(): Preferences? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setPreferences(preferences: Preferences?) {
    }

    actual fun getStats(): Stats? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setStats(stats: Stats?) {
    }

    actual fun getGemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getHourglassCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getCostume(): Outfit? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getEquipped(): Outfit? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun hasClass(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getCurrentMount(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getCurrentPet(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getSleep(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun hasParty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}