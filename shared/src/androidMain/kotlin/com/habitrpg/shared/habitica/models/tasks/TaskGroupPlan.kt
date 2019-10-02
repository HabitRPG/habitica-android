package com.habitrpg.shared.habitica.models.tasks


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class TaskGroupPlan : RealmObject() {

    @PrimaryKey
    internal actual var taskID: String? = null

    actual var approvalRequested: Boolean = false
    actual var approvalApproved: Boolean = false
    actual var approvalRequired: Boolean = false
}
