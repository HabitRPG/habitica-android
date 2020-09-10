package com.habitrpg.android.habitica.models

import com.facebook.internal.Mutable
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import java.util.*

class WorldState {

    var worldBossKey: String = ""
    var worldBossActive: Boolean = false
    var progress: QuestProgress? = null
    var rageStrikes: MutableList<QuestRageStrike>? = null

    var currentEventKey: String? = null
    var currentEventStartDate: Date? = null
    var currentEventEndDate: Date? = null
}