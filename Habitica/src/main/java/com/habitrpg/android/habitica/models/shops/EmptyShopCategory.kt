package com.habitrpg.android.habitica.models.shops

import android.content.Context
import com.habitrpg.android.habitica.R

class EmptyShopCategory(categoryIdentifier: String, context: Context?) {
    val title: String = context?.getString(R.string.you_own_all_items) ?: ""
    val description: String
    init {
        val stringId = when (categoryIdentifier) {
            "background" -> R.string.try_on_next_month
            "color" -> R.string.try_on_next_season
            "skin" -> R.string.try_on_next_season
            "mystery_sets" -> R.string.try_on_equipment
            else -> R.string.try_on_customize
        }
        description = context?.getString(stringId) ?: ""
    }
}
