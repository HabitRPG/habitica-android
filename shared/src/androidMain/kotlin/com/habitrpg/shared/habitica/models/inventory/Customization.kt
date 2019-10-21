package com.habitrpg.shared.habitica.models.inventory

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Customization : RealmObject() {
    @PrimaryKey
    actual var id: String? = null
    actual var identifier: String? = null
        set(identifier) {
            field = identifier
            this.updateID()
        }
    actual var category: String? = null
        set(category) {
            field = category
            this.updateID()
        }
    actual var type: String? = null
        set(type) {
            field = type
            this.updateID()
        }
    actual var notes: String? = null
    actual var customizationSet: String = ""
    actual var customizationSetName: String? = null
    actual var text: String? = null
    actual var purchased: Boolean = false
    actual var isBuyable: Boolean = false
    actual var price: Int = 0
    actual var setPrice: Int? = null
    actual var availableFrom: Date? = null
    actual var availableUntil: Date? = null

    //Not released yet
    //Discontinued
    actual val purchasable: Boolean
        get() {
            val today = Date()
            if (this.availableFrom != null && !this.availableFrom!!.before(today)) {
                return false
            }

            return !(this.availableUntil != null && !this.availableUntil!!.after(today))
        }

    actual val isUsable: Boolean
        get() = this.price == 0 || this.purchased

    actual val path: String
        get() {
            var path = this.type

            if (this.category != null) {
                path = path + "." + this.category
            }

            path = path + "." + this.identifier

            return path

        }

    actual fun updateID() {
        this.id = this.identifier + "_" + this.type + "_" + this.category
    }

    actual fun getImageName(userSize: String?, hairColor: String?): String {
        when (this.type) {
            "skin" -> return "skin_" + this.identifier!!
            "shirt" -> return userSize + "_shirt_" + this.identifier
            "hair" -> {
                if (this.identifier == "0") {
                    return "head_0"
                }

                when (this.category) {
                    "color" -> return "hair_bangs_1_" + this.identifier!!
                    "flower" -> return "hair_flower_" + this.identifier!!
                    else -> return "hair_" + this.category + "_" + this.identifier + "_" + hairColor
                }
            }
            "background" -> return "background_" + this.identifier!!
            "chair" -> return "chair_" + this.identifier!!
        }
        return ""
    }
}
