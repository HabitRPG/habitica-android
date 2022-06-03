package com.habitrpg.common.habitica.models.responses

class TaskDirectionDataTemp {

    var drop: TaskDirectionDataDrop? = null
    var quest: TaskDirectionDataQuest? = null
    var crit: Float? = null
}

class TaskDirectionDataQuest {
    var progressDelta: Double = 0.0
    var collection: Int = 0
}

class TaskDirectionDataDrop {

    var value: Int = 0
    var key: String? = null
    var type: String? = null
    var dialog: String? = null
}
