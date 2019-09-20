package com.habitrpg.shared.habitica.models.tasks


import com.habitrpg.shared.habitica.models.Tag

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by viirus on 08/08/15.
 */

actual open class TaskTag : RealmObject() {

    actual var tag: Tag? = null
        set(tag: Tag?) {
            field = tag
            tagId = tag?.id
            updatePrimaryKey()
        }
    actual var task: Task? = null
        set(task: Task?) {
            field = task
            taskId = task?.id
            updatePrimaryKey()
        }
    @PrimaryKey
    actual var id: String? = null
    actual var tagId: String? = ""
    actual var taskId: String? = ""

    actual fun updatePrimaryKey() {
        this.id = taskId + "_" + tagId
    }
}
