package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.models.Tag
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TaskTag : RealmObject() {
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

    @PrimaryKey
    var id: String? = null
    private var tagId = ""
    private var taskId: String? = ""

    private fun updatePrimaryKey() {
        id = taskId + "_" + tagId
    }
}