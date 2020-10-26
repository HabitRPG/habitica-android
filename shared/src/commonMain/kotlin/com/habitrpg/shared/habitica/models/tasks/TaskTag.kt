package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class TaskTag : NativeRealmObject() {
    var tag: Tag? = null
        set(value) {
            field = value
            tagId = tag?.id ?: ""
            updatePrimaryKey()
        }
    var task: Task? = null
        set(value) {
            field = value
            taskId = task?.id ?: ""
            updatePrimaryKey()
        }

    @PrimaryKeyAnnotation
    var id: String? = null
    private var tagId = ""
    private var taskId: String? = ""

    private fun updatePrimaryKey() {
        id = taskId + "_" + tagId
    }
}
