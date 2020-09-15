package com.habitrpg.shared.habitica.models.tasks


import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class TaskGroupPlan : NativeRealmObject() {

    @PrimaryKeyAnnotation
    internal var taskID: String? = null

    var approvalRequested: Boolean = false
    var approvalApproved: Boolean = false
    var approvalRequired: Boolean = false
}
