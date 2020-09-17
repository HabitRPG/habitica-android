package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by phillip on 25.07.17.
 */
open class QuestDropItem : RealmObject() {
    @PrimaryKey
    var combinedKey: String? = null
    var questKey: String? = null
    set(value) {
        field = value
        combinedKey = value + key
    }
    var key: String = ""
    set(value) {
        field = value
        combinedKey = questKey + value
    }
    var type: String? = null
    var text: String? = null
    var isOnlyOwner = false
    var count = 0

    val imageName: String
        get() = when (type) {
                "quests" -> "inventory_quest_scroll_$key"
                "eggs" -> "Pet_Egg_$key"
                "food" -> "Pet_Food_$key"
                "hatchingPotions" -> "Pet_HatchingPotion_$key"
                else -> "shop_$key"
            }
}