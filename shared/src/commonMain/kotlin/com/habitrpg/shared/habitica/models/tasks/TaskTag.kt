package com.habitrpg.shared.habitica.models.tasks


import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

/**
 * Created by viirus on 08/08/15.
 */

open class TaskTag : NativeRealmObject() {

    var tag: Tag? = null
        set(tag: Tag?) {
            field = tag
            tagId = field?.id
            updatePrimaryKey()
        }
    var task: Task? = null
        set(task: Task?) {
            field = task

            taskId = field?.id
            updatePrimaryKey()
        }

    @PrimaryKeyAnnotation
    var id: String? = null
    private var tagId: String? = ""
    private var taskId: String? = ""

    private fun updatePrimaryKey() {
        this.id = taskId + "_" + tagId
    }
}
