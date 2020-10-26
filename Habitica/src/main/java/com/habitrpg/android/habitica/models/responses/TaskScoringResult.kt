package com.habitrpg.android.habitica.models.responses

import com.habitrpg.shared.habitica.models.responses.TaskDirectionDataDrop

class TaskScoringResult {
    var drop: TaskDirectionDataDrop? = null
    var experienceDelta: Double? = null
    var healthDelta: Double? = null
    var goldDelta: Double? = null
    var manaDelta: Double? = null
    var hasLeveledUp: Boolean = false
    var questDamage: Double? = null
    var questItemsFound: Int? = null
}
