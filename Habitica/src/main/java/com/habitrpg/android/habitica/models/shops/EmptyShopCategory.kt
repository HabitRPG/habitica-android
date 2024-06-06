package com.habitrpg.android.habitica.models.shops

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R

class EmptyShopCategory(val categoryIdentifier: String, val shopIdentifier: String?, context: Context?) {
    val title: String = context?.getString(R.string.you_own_all_items) ?: ""
    val description: Spannable
    init {
        val stringId = when (categoryIdentifier) {
            "backgrounds" -> if (shopIdentifier == Shop.CUSTOMIZATIONS) R.string.try_on_next_month else R.string.try_on_customize
            "color" -> R.string.try_on_next_season
            "skin" -> R.string.try_on_next_season
            "mystery_sets" -> R.string.try_on_equipment
            else -> R.string.try_on_customize
        }
        if (context != null) {
            val descriptionString = context.getString(stringId)
            val words = listOf(context.getString(R.string.equipment), context.getString(R.string.customizing_your_avatar))
            val spannable = SpannableString(descriptionString)
            for (word in words) {
                val index = spannable.indexOf(word)
                if (index >= 0) {
                    spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_brand_neon)), index, index + word.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
            description = spannable
        } else {
            description = SpannableString("")
        }
    }
}
