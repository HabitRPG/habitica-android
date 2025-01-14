package com.habitrpg.android.habitica.models

class SetupCustomization {
    var key: String = ""
    var drawableId: Int? = null
    var colorId: Int? = null
    var text: String = ""
    var path: String = ""
    var category: String = ""
    var subcategory: String = ""

    companion object {
        fun createSize(
            key: String,
            drawableId: Int,
            text: String
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.text = text
            customization.path = "size"
            customization.category = "body"
            customization.subcategory = "size"
            return customization
        }

        fun createShirt(
            key: String,
            drawableId: Int
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.path = "shirt"
            customization.category = "body"
            customization.subcategory = "shirt"
            return customization
        }

        fun createSkin(
            key: String,
            colorId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.colorId = colorId
            customization.path = "skin"
            customization.category = "skin"
            return customization
        }

        fun createHairColor(
            key: String,
            colorId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.colorId = colorId
            customization.path = "hair.color"
            customization.category = "hair"
            customization.subcategory = "color"
            return customization
        }

        fun createHairBangs(
            key: String,
            drawableId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.path = "hair.bangs"
            customization.category = "hair"
            customization.subcategory = "bangs"
            return customization
        }

        fun createHairPonytail(
            key: String,
            drawableId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.path = "hair.base"
            customization.category = "hair"
            customization.subcategory = "base"
            return customization
        }

        fun createGlasses(
            key: String,
            drawableId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.path = "glasses"
            customization.category = "extras"
            customization.subcategory = "glasses"
            return customization
        }

        fun createFlower(
            key: String,
            drawableId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.path = "hair.flower"
            customization.category = "extras"
            customization.subcategory = "flower"
            return customization
        }

        fun createWheelchair(
            key: String,
            drawableId: Int?
        ): SetupCustomization {
            val customization = SetupCustomization()
            customization.key = key
            customization.drawableId = drawableId
            customization.path = "chair"
            customization.category = "extras"
            customization.subcategory = "wheelchair"
            return customization
        }
    }
}
