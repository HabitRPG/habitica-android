package com.habitrpg.android.habitica.models.shops

import android.content.res.Resources

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ShopItem : RealmObject() {
    @PrimaryKey
    var key: String = ""
    var text: String? = ""
    var notes: String? = ""
    @SerializedName("class")
    var imageName: String? = null
    get() {
        return if (field != null) {
            if (field!!.contains(" ")) {
                field!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            } else {
                field
            }
        } else {
            "shop_" + key
        }
    }

    var value: Int = 0
    var locked: Boolean = false
    var isLimited: Boolean = false
    var currency: String? = null
    var purchaseType: String = ""
    var categoryIdentifier: String = ""
    var limitedNumberLeft: Int? = null
    var unlockCondition: ShopItemUnlockCondition? = null
    var path: String? = null
    var isSuggested: String? = null
    var pinType: String? = null
    @SerializedName("klass")
    var habitClass: String? = null

    val isTypeItem: Boolean
        get() = "eggs" == purchaseType || "hatchingPotions" == purchaseType || "food" == purchaseType || "armoire" == purchaseType || "potion" == purchaseType

    val isTypeQuest: Boolean
        get() = "quests" == purchaseType

    val isTypeGear: Boolean
        get() = "gear" == purchaseType

    val isTypeAnimal: Boolean
        get() = "pets" == purchaseType || "mounts" == purchaseType

    fun canAfford(user: User?, canAlwaysAffordSpecial: Boolean): Boolean = when(currency) {
        "gold" -> value <= user?.stats?.gp ?: 0.0
        "gems" -> if (canAlwaysAffordSpecial) true else value <= user?.gemCount ?: 0
        "hourglasses" -> if (canAlwaysAffordSpecial) true else value <= user?.purchased?.plan?.consecutive?.trinkets ?: 0
        else -> false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && ShopItem::class.java.isAssignableFrom(other.javaClass)) {
            val otherItem = other as? ShopItem
            return this.key == otherItem?.key
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this.key.hashCode()
    }

    companion object {

        private const val GEM_FOR_GOLD = "gem"

        fun makeGemItem(res: Resources?): ShopItem {
            val item = ShopItem()
            item.key = GEM_FOR_GOLD
            item.text = res?.getString(R.string.gem_shop) ?: ""
            item.notes = res?.getString(R.string.gem_for_gold_description) ?: ""
            item.imageName = "gem_shop"
            item.value = 20
            item.currency = "gold"
            item.purchaseType = "gems"
            return item
        }
    }
}
