package com.habitrpg.shared.habitica.models.tasks


import com.habitrpg.shared.habitica.models.Tag

/**
 * Created by viirus on 08/08/15.
 */

expect open class TaskTag {
    var tag: Tag?
    var task: Task?
    var id: String?
    var tagId: String?
    var taskId: String?

    fun updatePrimaryKey()
}
