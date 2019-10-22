package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.NativeList

expect open class Items {

    var userId: String?
    var eggs: NativeList<OwnedItem>?
    var food: NativeList<OwnedItem>?
    var hatchingPotions: NativeList<OwnedItem>?
    var quests: NativeList<OwnedItem>?
    var pets: NativeList<OwnedPet>?
    var mounts: NativeList<OwnedMount>?
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
