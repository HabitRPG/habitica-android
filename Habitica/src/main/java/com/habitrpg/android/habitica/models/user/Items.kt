package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarItems
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date

@RealmClass(embedded = true)
open class Items : RealmObject, BaseObject, AvatarItems {
    fun setItemTypes() {
        hatchingPotions?.forEach { it.itemType = "hatchingPotions" }
        eggs?.forEach { it.itemType = "eggs" }
        food?.forEach { it.itemType = "food" }
        quests?.forEach { it.itemType = "quests" }
        special?.forEach { it.itemType = "special" }
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
    var special: RealmList<OwnedItem>? = null
        set(value) {
            field = value
            field?.forEach { it.itemType = "special" }
        }

    var pets: RealmList<OwnedPet>? = null
    var mounts: RealmList<OwnedMount>? = null
    override var currentMount: String? = null
    override var currentPet: String? = null
    var lastDropCount: Int = 0
    var lastDropDate: Date? = null

    // private QuestContent quest;
    override var gear: Gear? = null

    constructor(currentMount: String, currentPet: String, lastDropCount: Int, lastDropDate: Date) {
        this.currentMount = currentMount
        this.currentPet = currentPet
        this.lastDropCount = lastDropCount
        this.lastDropDate = lastDropDate
    }

    constructor()

    val hasTransformationItems: Boolean
        get() {
            return special?.any { transformationItem ->
                transformationItem.key == ("seafoam") && transformationItem.numberOwned > 0 ||
                    transformationItem.key == ("shinySeed") && transformationItem.numberOwned > 0 ||
                    transformationItem.key == ("snowball") && transformationItem.numberOwned > 0 ||
                    transformationItem.key == ("spookySparkles") && transformationItem.numberOwned > 0
            } ?: false
        }
}
