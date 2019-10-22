package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.HabitRpgClass

actual open class Stats actual constructor() {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    internal actual var user: User?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var constitution: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var strength: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var per: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var intelligence: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var training: Training?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var buffs: Buffs?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var points: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var lvl: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var habitClass: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var gp: Double?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var exp: Double?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var mp: Double?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var hp: Double?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var toNextLevel: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var maxHealth: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var maxMP: Int?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual val isBuffed: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun merge(stats: Stats?) {
    }

    actual fun setHabitClass(habitRpgClass: HabitRpgClass) {
    }

    actual companion object {
        actual val STRENGTH: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        actual val INTELLIGENCE: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        actual val CONSTITUTION: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        actual val PERCEPTION: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    }

}