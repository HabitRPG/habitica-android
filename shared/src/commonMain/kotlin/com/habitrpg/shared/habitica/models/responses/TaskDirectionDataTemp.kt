package com.habitrpg.shared.habitica.models.responses

import com.habitrpg.shared.habitica.HParcelable
import com.habitrpg.shared.habitica.HParcelize

class TaskDirectionDataTemp {

    var drop: TaskDirectionDataDrop? = null
    var quest: TaskDirectionDataQuest? = null
    var crit: Float? = null
}

class TaskDirectionDataQuest {
    var progressDelta: Double = 0.0
    var collection: Int = 0
}

@HParcelize
data class TaskDirectionDataDrop(
    var value: Int,
    var key: String?,
    var type: String?,
    var dialog: String?,
): HParcelable
