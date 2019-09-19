package com.habitrpg.shared.habitica.models.tasks

expect class TaskGroupPlan {
    internal var taskID: String?

    var approvalRequested: Boolean
    var approvalApproved: Boolean
    var approvalRequired: Boolean
}