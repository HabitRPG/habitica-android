package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User

interface SetupCustomizationRepository {
    fun getCustomizations(
        type: String,
        user: User
    ): List<SetupCustomization>

    fun getCustomizations(
        type: String,
        subtype: String?,
        user: User
    ): List<SetupCustomization>

    companion object {
        const val CATEGORY_BODY = "body"
        const val CATEGORY_SKIN = "skin"
        const val CATEGORY_HAIR = "hair"
        const val CATEGORY_EXTRAS = "extras"

        const val SUBCATEGORY_SIZE = "size"
        const val SUBCATEGORY_SHIRT = "shirt"
        const val SUBCATEGORY_COLOR = "color"
        const val SUBCATEGORY_PONYTAIL = "base"
        const val SUBCATEGORY_BANGS = "bangs"
        const val SUBCATEGORY_FLOWER = "flower"
        const val SUBCATEGORY_WHEELCHAIR = "wheelchair"
        const val SUBCATEGORY_GLASSES = "glasses"
    }
}
