package com.habitrpg.shared.habitica.models.responses

import com.habitrpg.shared.habitica.HParcelable
import com.habitrpg.shared.habitica.HParcelize
import com.habitrpg.shared.habitica.models.AvatarStats

@HParcelize
class TaskScoringResult(): HParcelable {
    constructor(data: TaskDirectionData, stats: AvatarStats?) : this() {
        hasLeveledUp = data.lvl > (stats?.lvl ?: 0)
        healthDelta = data.hp - (stats?.hp ?: 0.0)
        hasDied = data.hp <= 0.0
        experienceDelta = if (hasLeveledUp) {
            (stats?.toNextLevel ?: 0).toDouble() - (stats?.exp ?: 0.0) + data.exp
        } else {
            data.exp - (stats?.exp ?: 0.0)
        }
        manaDelta = data.mp - (stats?.mp ?: 0.0)
        goldDelta = data.gp - (stats?.gp ?: 0.0)
        level = data.lvl
        questDamage = data._tmp?.quest?.progressDelta
        questItemsFound = data._tmp?.quest?.collection
        drop = data._tmp?.drop
    }

    var hasDied: Boolean = false
    var drop: TaskDirectionDataDrop? = null
    var experienceDelta: Double? = null
    var healthDelta: Double? = null
    var goldDelta: Double? = null
    var manaDelta: Double? = null
    var hasLeveledUp: Boolean = false
    var level: Int? = null
    var questDamage: Double? = null
    var questItemsFound: Int? = null
}
