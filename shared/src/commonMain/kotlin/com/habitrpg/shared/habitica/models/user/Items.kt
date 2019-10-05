package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList

expect open class Items {

    var userId: String?
    var eggs: NativeRealmList<OwnedItem>?
    var food: NativeRealmList<OwnedItem>?
    var hatchingPotions: NativeRealmList<OwnedItem>?
    var quests: NativeRealmList<OwnedItem>?
    var pets: NativeRealmList<OwnedPet>?
    var mounts: NativeRealmList<OwnedMount>?
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
