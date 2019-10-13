package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.nativeLibraries.RealmList

actual open class Quest {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var key: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var active: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var leader: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var RSVPNeeded: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var members: RealmList<QuestMember>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var progress: QuestProgress?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var participants: RealmList<Member>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var rageStrikes: RealmList<QuestRageStrike>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual fun hasRageStrikes(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun addRageStrike(rageStrike: QuestRageStrike) {
    }

    actual val activeRageStrikeNumber: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}