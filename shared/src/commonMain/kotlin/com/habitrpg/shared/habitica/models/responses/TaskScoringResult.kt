package com.habitrpg.shared.habitica.models.responses

import com.habitrpg.shared.habitica.HParcelable
import com.habitrpg.shared.habitica.HParcelize
import com.habitrpg.shared.habitica.models.AvatarStats

@HParcelize
data class TaskScoringResult(
    var hasDied: Boolean = false,
    var drop: TaskDirectionDataDrop? = null,
    var experienceDelta: Double = 0.0,
    var healthDelta: Double = 0.0,
    var goldDelta: Double = 0.0,
    var manaDelta: Double = 0.0,
    var hasLeveledUp: Boolean = false,
    var level: Int = 0,
    var questDamage: Double? = null,
    var questItemsFound: Int? = null
) : HParcelable {
    constructor(data: TaskDirectionData, stats: AvatarStats?) : this(
        data.hp <= 0.0,
        data._tmp?.drop,
        if (data.lvl > (stats?.lvl ?: 0)) {
            (stats?.toNextLevel ?: 0).toDouble() - (stats?.exp ?: 0.0) + data.exp
        } else {
            data.exp - (stats?.exp ?: 0.0)
        },
        data.hp - (stats?.hp ?: 0.0),
        data.gp - (stats?.gp ?: 0.0),
        data.mp - (stats?.mp ?: 0.0),
        data.lvl > (stats?.lvl ?: 0),
        data.lvl,
        data._tmp?.quest?.progressDelta,
        data._tmp?.quest?.collection
    )
}
