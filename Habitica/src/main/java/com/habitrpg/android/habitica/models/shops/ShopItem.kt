package com.habitrpg.android.habitica.models.shops

import android.content.Context
import android.content.res.Resources
import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.ItemEvent
import com.habitrpg.android.habitica.models.user.User
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

open class ShopItem : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String = ""
    var text: String? = ""
    var notes: String? = ""

    @SerializedName("class")
    var imageName: String? = null
        get() {
            val name = if (field != null) {
                if (field!!.contains(" ")) {
                    field!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                } else {
                    field
                }
            } else {
                "shop_$key"
            }
            return if (purchaseType == "customization" && name?.startsWith("icon_") == false) {
                "icon_$name"
            } else {
                name
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
    var unlockPath: String? = null
    var isSuggested: String? = null
    var pinType: String? = null

    @SerializedName("klass")
    var habitClass: String? = null
    var specialClass: String? = null
    var previous: String? = null

    @SerializedName("lvl")
    var level: Int? = null
    var event: ItemEvent? = null

    @SerializedName("end")
    var endDate: Date? = null

    val availableUntil: Date?
        get() {
            return endDate ?: event?.end
        }

    var setImageNames = RealmList<String>()

    val isTypeItem: Boolean
        get() = "eggs" == purchaseType || "hatchingPotions" == purchaseType || "food" == purchaseType || "armoire" == purchaseType || "potion" == purchaseType || "debuffPotion" == purchaseType || "fortify" == purchaseType

    val isTypeSpecial: Boolean
        get() = "special" == purchaseType

    val isTypeBundle: Boolean
        get() = "bundles" == purchaseType

    val isTypeQuest: Boolean
        get() = "quests" == purchaseType

    val isTypeGear: Boolean
        get() = "gear" == purchaseType

    val isTypeAnimal: Boolean
        get() = "pets" == purchaseType || "mounts" == purchaseType

    val canPurchaseBulk: Boolean
        get() = "eggs" == purchaseType || "hatchingPotions" == purchaseType || "food" == purchaseType || "gems" == purchaseType

    fun canAfford(
        user: User?,
        quantity: Int
    ): Boolean =
        when (currency) {
            "gold" -> (value * quantity) <= (user?.stats?.gp ?: 0.0)
            "gems" -> (value * quantity) <= (user?.gemCount ?: 0)
            "hourglasses" -> (value * quantity) <= (user?.hourglassCount ?: 0)
            else -> true
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
            item.pinType = "gem"
            item.path = "special.gems"
            return item
        }

        fun makeFortifyItem(res: Resources?): ShopItem {
            val item = ShopItem()
            item.key = "fortify"
            item.text = res?.getString(R.string.fortify_shop) ?: ""
            item.notes = res?.getString(R.string.fortify_shop_description) ?: ""
            item.imageName = "inventory_special_fortify"
            item.value = 4
            item.currency = "gems"
            item.pinType = "fortify"
            item.path = "special.fortify"
            item.purchaseType = "fortify"
            return item
        }

        private fun isFreeRebirth(user: User?): Boolean {
            val userLevel = user?.stats?.lvl ?: 0
            if (userLevel < 100) return false

            val lastFreeRebirth = user?.flags?.lastFreeRebirth ?: return true
            val now = Date()
            val diffInMillis = now.time - lastFreeRebirth.time
            val daysSinceLastFreeRebirth = diffInMillis / (1000 * 60 * 60 * 24)

            return daysSinceLastFreeRebirth >= 45
        }

        fun makeRebirthItem(res: Resources?, user: User?): ShopItem {
            val item = ShopItem()
            item.key = "rebirth_orb"
            item.text = res?.getString(R.string.rebirth_shop) ?: ""
            item.notes = res?.getString(R.string.rebirth_shop_description) ?: ""
            item.imageName = "rebirth_orb"
            item.value = if (isFreeRebirth(user)) 0 else 6
            item.currency = "gems"
            item.pinType = "rebirth_orb"
            item.path = "special.rebirth_orb"
            item.purchaseType = "rebirth_orb"
            return item
        }

        fun fromCustomization(
            customization: Customization,
            userSize: String?,
            hairColor: String?
        ): ShopItem {
            val item = ShopItem()
            item.key = customization.identifier ?: ""
            item.text = customization.text
            item.currency = "gems"
            item.notes = customization.notes
            item.value = customization.price ?: 0
            item.path = customization.path
            item.unlockPath = customization.unlockPath
            item.pinType = customization.type
            if (customization.type == "background") {
                item.purchaseType = "background"
                item.imageName = customization.getImageName(userSize, hairColor)
            } else {
                item.purchaseType = "customization"
                item.imageName = customization.getIconName(userSize, hairColor)
            }
            return item
        }

        fun fromCustomizationSet(
            set: CustomizationSet,
            additionalSetItems: List<Customization>?,
            userSize: String?,
            hairColor: String?
        ): ShopItem {
            val item = ShopItem()
            var path = ""
            for (customization in set.customizations) {
                path = path + "," + customization.unlockPath
                item.setImageNames.add(customization.getIconName(userSize, hairColor))
            }
            for (customization in additionalSetItems ?: emptyList()) {
                path = path + "," + customization.unlockPath
                item.setImageNames.add(customization.getIconName(userSize, hairColor))
            }
            if (path.isEmpty()) {
                item.unlockPath = path
            } else {
                item.unlockPath = path.substring(1)
            }
            item.text = set.text
            item.key = set.identifier ?: ""
            item.currency = "gems"
            item.value = set.price
            item.purchaseType = "customizationSet"
            if (set.customizations.firstOrNull()?.type == "background") {
                // TODO: Needs a way to be translated.
                item.notes = "Get all three Backgrounds in this bundle."
            }
            return item
        }

        fun fromAnimalEquipment(equipment: Equipment?): ShopItem {
            val item = ShopItem()
            item.key = equipment?.key ?: ""
            item.text = equipment?.text
            item.currency = "gems"
            item.value = 2
            item.purchaseType = "gear"
            item.imageName = equipment?.key
            return item
        }
    }
}
