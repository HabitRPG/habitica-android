package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper

expect open class Items {

    var userId: String?
    var eggs: RealmListWrapper<OwnedItem>?
    var food: RealmListWrapper<OwnedItem>?
    var hatchingPotions: RealmListWrapper<OwnedItem>?
    var quests: RealmListWrapper<OwnedItem>?
    var pets: RealmListWrapper<OwnedPet>?
    var mounts: RealmListWrapper<OwnedMount>?
    var currentMount: String?
    var currentPet: String?
    var lastDrop_count: Int
    var lastDrop_date: NativeDate?

    //private QuestContent quest;
    var gear: Gear?
    var special: SpecialItems?

    constructor(currentMount: String, currentPet: String, lastDrop_count: Int, lastDrop_date: NativeDate)

    constructor()
}
