package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.nativeLibraries.RealmList

actual open class Gear {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var owned: RealmList<Equipment>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    internal actual var items: Items?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var equipped: Outfit?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var costume: Outfit?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

}