package com.habitrpg.android.habitica.models.user

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Items : RealmObject {

    @PrimaryKey
    var userId: String? = null
        set(userId) {
            field = userId
            if (gear != null && gear?.isManaged == false) {
                gear?.userId = userId
            }
            if (special != null && special?.isManaged == false) {
                special?.userId = userId
            }
            eggs?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                    it.itemType = "eggs"
                }
            }
            food?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                    it.itemType = "food"
                }
            }
            hatchingPotions?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                    it.itemType = "hatchingPotions"
                }
            }
            quests?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                    it.itemType = "quests"
                }
            }
            pets?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                }
            }
            mounts?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                }
            }
        }
    var eggs: RealmList<OwnedItem>? = null
    var food: RealmList<OwnedItem>? = null
    var hatchingPotions: RealmList<OwnedItem>? = null
    var quests: RealmList<OwnedItem>? = null
    var pets: RealmList<OwnedPet>? = null
    var mounts: RealmList<OwnedMount>? = null
    var currentMount: String? = null
    var currentPet: String? = null
    var lastDrop_count: Int = 0
    var lastDrop_date: Date? = null

    //private QuestContent quest;
    var gear: Gear? = null
    var special: SpecialItems? = null

    constructor(currentMount: String, currentPet: String, lastDrop_count: Int, lastDrop_date: Date) {
        this.currentMount = currentMount
        this.currentPet = currentPet
        this.lastDrop_count = lastDrop_count
        this.lastDrop_date = lastDrop_date
    }

    constructor()
}
