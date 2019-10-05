package com.habitrpg.shared.habitica.models.user


import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

actual open class Items : RealmObject {

    @PrimaryKey
    actual var userId: String? = null
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
    actual var eggs: RealmList<OwnedItem>? = null
    actual var food: RealmList<OwnedItem>? = null
    actual var hatchingPotions: RealmList<OwnedItem>? = null
    actual var quests: RealmList<OwnedItem>? = null
    actual var pets: RealmList<OwnedPet>? = null
    actual var mounts: RealmList<OwnedMount>? = null
    actual var currentMount: String? = null
    actual var currentPet: String? = null
    actual var lastDrop_count: Int = 0
    actual var lastDrop_date: Date? = null

    //private QuestContent quest;
    actual var gear: Gear? = null
    actual var special: SpecialItems? = null

    actual constructor(currentMount: String, currentPet: String, lastDrop_count: Int, lastDrop_date: Date) {
        this.currentMount = currentMount
        this.currentPet = currentPet
        this.lastDrop_count = lastDrop_count
        this.lastDrop_date = lastDrop_date
    }

    actual constructor()
}
