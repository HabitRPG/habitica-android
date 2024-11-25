package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestDropItem : RealmObject(), BaseObject {
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
    var onlyOwner = false
    var count = 0

    val imageName: String
        get() =
            when (type) {
                "quests" -> "inventory_quest_scroll_$key"
                "eggs" -> "Pet_Egg_$key"
                "food" -> "Pet_Food_$key"
                "hatchingPotions" -> "Pet_HatchingPotion_$key"
                "pets" -> "stable_Pet-$key"
                "mounts" -> "Mount_Head_$key"
                else -> "shop_$key"
            }
}
