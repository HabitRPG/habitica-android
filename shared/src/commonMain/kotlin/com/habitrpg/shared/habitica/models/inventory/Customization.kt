package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Customization : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var id: String = ""
    var identifier: String? = null
        set(identifier) {
            field = identifier
            this.updateID()
        }
    var category: String? = null
        set(category) {
            field = category
            this.updateID()
        }
    var type: String? = null
        set(type) {
            field = type
            this.updateID()
        }
    var notes: String? = null
    var customizationSet: String? = null
    var customizationSetName: String? = null
    var text: String? = null
    var purchased: Boolean = false
    var isBuyable: Boolean = false
    var price: Int? = null
    var setPrice: Int? = null
    var availableFrom: NativeDate? = null
    var availableUntil: NativeDate? = null

    //Not released yet
    //Discontinued
    val purchasable: Boolean
        get() {
            val today = NativeDate()
            if (this.availableFrom != null && !this.availableFrom!!.before(today)) {
                return false
            }

            return !(this.availableUntil != null && !this.availableUntil!!.after(today))
        }

    fun getIconName(userSize: String?, hairColor: String?): String {
        return if (type == "background") {
            "icon_background_$identifier"
        } else {
            getImageName(userSize, hairColor)
        }
    }

    fun getImageName(userSize: String?, hairColor: String?): String {
        when (type) {
            "skin" -> return "skin_$identifier"
            "shirt" -> return userSize + "_shirt_" + identifier
            "hair" -> {
                return if (identifier == "0") {
                    "head_0"
                } else when (this.category) {
                    "color" -> "hair_bangs_1_$identifier"
                    "flower" -> "hair_flower_$identifier"
                    else -> "hair_" + this.category + "_" + identifier + "_" + hairColor
                }
            }
            "background" -> return "background_$identifier"
            "chair" -> return "chair_$identifier"
        }
        return ""
    }

    fun isUsable(purchased: Boolean): Boolean {
        return price == null || price == 0 || purchased
    }

    val path: String
        get() {
            var path = this.type

            if (this.category != null) {
                path = path + "." + this.category
            }

            path = path + "." + this.identifier

            return path

        }

    private fun updateID() {
        this.id = this.identifier + "_" + this.type + "_" + this.category
    }
}
