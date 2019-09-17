package com.habitrpg.android.habitica.models.responses

import com.habitrpg.shared.habitica.models.responses.TaskDirectionDataDrop
import com.habitrpg.shared.habitica.models.responses.TaskDirectionDataQuest

class TaskDirectionDataTemp {

    var drop: TaskDirectionDataDrop? = null
    var quest: TaskDirectionDataQuest? = null
    var crit: Float? = null
}

class TaskDirectionDataQuest {
    var progressDelta: Double = 0.0
}

class TaskDirectionDataDrop {

    var value: Int = 0
    var key: String? = null
    var type: String? = null
    var dialog: String? = null

}