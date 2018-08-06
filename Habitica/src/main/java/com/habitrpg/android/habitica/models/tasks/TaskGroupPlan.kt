package com.habitrpg.android.habitica.models.tasks


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TaskGroupPlan : RealmObject() {

    @PrimaryKey
    internal var taskID: String? = null

    var approvalRequested: Boolean = false
    var approvalApproved: Boolean = false
    var approvalRequired: Boolean = false
}
