package com.habitrpg.shared.habitica.models.tasks

interface BaseTask {
    val completed: Boolean
    var type: TaskType?
    var isDue: Boolean?
    var streak: Int?
    var counterDown: Int?
    var counterUp: Int?

    val isDisplayedActive: Boolean
        get() = ((isDue == true && type == TaskType.DAILY) || type == TaskType.TODO) && !completed
}

val BaseTask.streakString: String?
    get() {
        return if ((counterUp ?: 0) > 0 && (counterDown ?: 0) > 0) {
            "+" + counterUp.toString() + " | -" + counterDown?.toString()
        } else if ((counterUp ?: 0) > 0) {
            "+" + counterUp.toString()
        } else if ((counterDown ?: 0) > 0) {
            "-" + counterDown.toString()
        } else if ((streak ?: 0) > 0) {
            return streak.toString()
        } else {
            null
        }
    }
