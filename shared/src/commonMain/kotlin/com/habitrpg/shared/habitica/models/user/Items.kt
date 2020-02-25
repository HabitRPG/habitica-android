package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Items : NativeRealmObject {

    @PrimaryKeyAnnotation
    var userId: String? = null
        set(userId) {
            field = userId
            if (gear != null && gear?.isManaged() == false) {
                gear?.userId = userId
            }
            if (special != null && special?.isManaged() == false) {
                special?.userId = userId
            }
            eggs?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                    it.itemType = "eggs"
                }
            }
            food?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                    it.itemType = "food"
                }
            }
            hatchingPotions?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                    it.itemType = "hatchingPotions"
                }
            }
            quests?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                    it.itemType = "quests"
                }
            }
            pets?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                }
            }
            mounts?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                }
            }
        }
    var eggs: NativeList<OwnedItem>? = null
    var food: NativeList<OwnedItem>? = null
    var hatchingPotions: NativeList<OwnedItem>? = null
    var quests: NativeList<OwnedItem>? = null
    var pets: NativeList<OwnedPet>? = null
    var mounts: NativeList<OwnedMount>? = null
    var currentMount: String? = null
    var currentPet: String? = null
    var lastDrop_count: Int = 0
    var lastDrop_date: NativeDate? = null

    //private QuestContent quest;
    var gear: Gear? = null
    var special: SpecialItems? = null

    constructor(currentMount: String, currentPet: String, lastDrop_count: Int, lastDrop_date: NativeDate) {
        this.currentMount = currentMount
        this.currentPet = currentPet
        this.lastDrop_count = lastDrop_count
        this.lastDrop_date = lastDrop_date
    }

    constructor()
}
