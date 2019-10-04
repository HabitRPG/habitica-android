package com.habitrpg.shared.habitica.models.members

import android.content.Context
import androidx.core.content.ContextCompat
import space.thelen.shared.cluetective.R


actual class PlayerTier actual constructor(actual var title: String, actual var id: Int) {

    actual companion object {
        actual fun getTiers(): List<PlayerTier> {
            return arrayListOf(
                    PlayerTier("Tier 1 (Friend)", 1),
                    PlayerTier("Tier 2 (Friend)", 2),
                    PlayerTier("Tier 3 (Elite)", 3),
                    PlayerTier("Tier 4 (Elite)", 4),
                    PlayerTier("Tier 5 (Champion)", 5),
                    PlayerTier("Tier 6 (Champion)", 6),
                    PlayerTier("Tier 7 (Legendary)", 7),
                    PlayerTier("Moderator (Guardian)", 8),
                    PlayerTier("Staff (Heroic)", 9)
            )
        }

        actual fun getColorForTier(context: Context, value: Int): Int {
            return when (value) {
                1 -> ContextCompat.getColor(context, R.color.contributor_1)
                2 -> ContextCompat.getColor(context, R.color.contributor_2)
                3 -> ContextCompat.getColor(context, R.color.contributor_3)
                4 -> ContextCompat.getColor(context, R.color.contributor_4)
                5 -> ContextCompat.getColor(context, R.color.contributor_5)
                6 -> ContextCompat.getColor(context, R.color.contributor_6)
                7 -> ContextCompat.getColor(context, R.color.contributor_7)
                8 -> ContextCompat.getColor(context, R.color.contributor_mod)
                9 -> ContextCompat.getColor(context, R.color.contributor_staff)
                else -> ContextCompat.getColor(context, R.color.contributor_0)
            }
        }
    }
}