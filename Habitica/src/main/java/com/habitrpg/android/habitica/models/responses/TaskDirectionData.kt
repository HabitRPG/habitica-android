package com.habitrpg.android.habitica.models.responses

/**
 * This class represent the data sent back from the API when calling /user/tasks/{id}/{direction}.
 * It holds almost the same thing as Stats, except toNextLevel & maxHealth & maxHP.
 * It also holds a delta, which represent the task value modification.
 * Created by MagicMicky on 12/06/2014.
 */
class TaskDirectionData {
    var delta: Float = 0.toFloat()
    var _tmp: TaskDirectionDataTemp? = null
    var exp: Double = 0.0
    var hp: Double = 0.0
    var gp: Double = 0.0
    var mp: Double = 0.0
    var lvl: Int = 0
}
