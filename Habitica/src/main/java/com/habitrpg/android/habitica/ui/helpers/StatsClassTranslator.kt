package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.user.Stats
import com.habitrpg.shared.habitica.models.user.Stats.Companion.HEALER
import com.habitrpg.shared.habitica.models.user.Stats.Companion.ROGUE
import com.habitrpg.shared.habitica.models.user.Stats.Companion.WARRIOR
import com.habitrpg.shared.habitica.models.user.Stats.Companion.MAGE


object StatsLanguages {
    fun getTranslatedClassName(stats: Stats?, context: Context): String? {
        return when (stats?.habitClass) {
            HEALER -> context.getString(R.string.healer)
            ROGUE -> context.getString(R.string.rogue)
            WARRIOR -> context.getString(R.string.warrior)
            MAGE -> context.getString(R.string.mage)
            else -> context.getString(R.string.warrior)
        }
    }
}
