package com.habitrpg.android.habitica.models.shops

import android.content.Context
import android.content.res.Resources

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ShopItem : RealmObject() {
    @PrimaryKey
    var key: String = ""
    set(value) {
        field = value
        unlockCondition?.questKey = key
    }
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
    set(value) {
        field = value
        if (key.isNotEmpty()) {
            field?.questKey = key
        }
    }
    var path: String? = null
    var isSuggested: String? = null
    var pinType: String? = null
    @SerializedName("klass")
    var habitClass: String? = null
    var previous: String? = null
    @SerializedName("lvl")
    var level: Int? = null

    val isTypeItem: Boolean
        get() = "eggs" == purchaseType || "hatchingPotions" == purchaseType || "food" == purchaseType || "armoire" == purchaseType || "potion" == purchaseType

    val isTypeQuest: Boolean
        get() = "quests" == purchaseType

    val isTypeGear: Boolean
        get() = "gear" == purchaseType

    val isTypeAnimal: Boolean
        get() = "pets" == purchaseType || "mounts" == purchaseType

    fun canAfford(user: User?, quantity: Int): Boolean = when(currency) {
        "gold" -> (value * quantity) <= user?.stats?.gp ?: 0.0
        "gems" -> true
        "hourglasses" -> true
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

    fun shortLockedReason(context: Context): String? {
        return when {
            unlockCondition != null -> {
                unlockCondition?.shortReadableUnlockCondition(context)
            }
            previous != null -> {
                try {
                    val thisNumber = Character.getNumericValue(key.last())
                    context.getString(R.string.unlock_previous_short, thisNumber - 1)
                } catch (e: NumberFormatException) {
                    null
                }
            }
            level != null -> {
                context.getString(R.string.level_unabbreviated, level ?: 0)
            }
            else -> null
        }
    }

    fun lockedReason(context: Context): String? {
        return when {
            unlockCondition != null -> {
                unlockCondition?.readableUnlockCondition(context)
            }
            previous != null -> {
                try {
                    val thisNumber = Character.getNumericValue(key.last())
                    context.getString(R.string.unlock_previous, thisNumber - 1)
                } catch (e: NumberFormatException) {
                    null
                }
            }
            level != null -> {
                context.getString(R.string.unlock_level, level ?: 0)
            }
            else -> null
        }
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
