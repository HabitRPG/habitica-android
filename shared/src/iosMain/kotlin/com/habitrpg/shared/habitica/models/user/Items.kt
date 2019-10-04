package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper

actual open class Items {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var eggs: RealmListWrapper<OwnedItem>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var food: RealmListWrapper<OwnedItem>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var hatchingPotions: RealmListWrapper<OwnedItem>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var quests: RealmListWrapper<OwnedItem>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var pets: RealmListWrapper<OwnedPet>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var mounts: RealmListWrapper<OwnedMount>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var currentMount: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var currentPet: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var lastDrop_count: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var lastDrop_date: NativeDate?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var gear: Gear?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var special: SpecialItems?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual constructor(currentMount: String, currentPet: String, lastDrop_count: Int, lastDrop_date: NativeDate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}