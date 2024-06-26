package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

open class Customization : RealmObject(), BaseObject {
    @PrimaryKey
    var id: String? = null
    var identifier: String? = null
        set(value) {
            field = value
            updateID()
        }
    var category: String? = null
        set(value) {
            field = value
            updateID()
        }
    var type: String? = null
        set(value) {
            field = value
            updateID()
        }
    var notes: String? = null
    var customizationSet: String? = null
    var customizationSetName: String? = null
    var text: String? = null
    var isBuyable = false
    var price: Int? = null
    var setPrice: Int? = null
    var availableFrom: Date? = null
    var availableUntil: Date? = null

    private fun updateID() {
        id = identifier + "_" + type + "_" + this.category
    }

    // Not released yet
    val purchasable: Boolean
        get() {
            val today = Date()
            if (availableFrom != null && !availableFrom!!.before(today)) { // Not released yet
                return false
            }
            return !(availableUntil != null && !availableUntil!!.after(today))
        }

    fun getIconName(
        userSize: String?,
        hairColor: String?
    ): String? {
        if (this.type == "hair" && this.category == "color") {
            return "icon_color_hair_bangs_1_$identifier"
        }
        val name = (getImageName(userSize, hairColor) ?: return null)
        return if (!name.startsWith("icon_")) "icon_$name" else name
    }

    fun getImageName(
        userSize: String?,
        hairColor: String?
    ): String? {
        if (!this.isValid) {
            return null
        }
        if (identifier?.isNotBlank() != true || identifier == "none" || identifier == "0") return null
        return when (type) {
            "skin" -> return "skin_$identifier"
            "shirt" -> return userSize + "_shirt_" + identifier
            "hair" -> {
                when (this.category) {
                    "color" -> "hair_bangs_1_$identifier"
                    "flower" -> "hair_flower_$identifier"
                    else -> "hair_" + this.category + "_" + identifier + "_" + hairColor
                }
            }

            "background" -> return "background_$identifier"
            "chair" -> return "chair_$identifier"
            else -> null
        }
    }

    fun isUsable(purchased: Boolean): Boolean {
        return price == null || price == 0 || purchased
    }

    val path: String
        get() {
            var path = if (type == "background") "backgrounds.backgrounds" else type
            if (this.customizationSet != null) {
                path =
                    if (type == "background") {
                        path +
                            this.customizationSet?.substring(
                                5,
                                7
                            ) + this.customizationSet?.substring(0, 4)
                    } else {
                        path + "." + this.customizationSet
                    }
            } else if (this.category != null) {
                path = path + "." + this.category
            }
            path = "$path.$identifier"
            return path
        }

    val unlockPath: String
        get() {
            var path = type
            if (this.category != null) {
                path = path + "." + this.category
            }
            path = "$path.$identifier"
            return path
        }
}
