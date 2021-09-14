package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass(embedded = true)
open class Items : RealmObject, BaseObject {
    fun setItemTypes() {
        hatchingPotions?.forEach { it.itemType = "hatchingPotions" }
        eggs?.forEach { it.itemType = "eggs" }
        food?.forEach { it.itemType = "food" }
        quests?.forEach { it.itemType = "quests" }
    }

    var eggs: RealmList<OwnedItem>? = null
        set(value) {
            field = value
            field?.forEach { it.itemType = "eggs" }
        }
    var food: RealmList<OwnedItem>? = null
        set(value) {
            field = value
            field?.forEach { it.itemType = "food" }
        }
    var hatchingPotions: RealmList<OwnedItem>? = null
        set(value) {
            field = value
            field?.forEach { it.itemType = "hatchingPotions" }
        }
    var quests: RealmList<OwnedItem>? = null
        set(value) {
            field = value
            field?.forEach { it.itemType = "quests" }
        }
    var pets: RealmList<OwnedPet>? = null
    var mounts: RealmList<OwnedMount>? = null
    var currentMount: String? = null
    var currentPet: String? = null
    var lastDropCount: Int = 0
    var lastDropDate: Date? = null

    // private QuestContent quest;
    var gear: Gear? = null
    var special: SpecialItems? = null

    constructor(currentMount: String, currentPet: String, lastDropCount: Int, lastDropDate: Date) {
        this.currentMount = currentMount
        this.currentPet = currentPet
        this.lastDropCount = lastDropCount
        this.lastDropDate = lastDropDate
    }

    constructor()
}
