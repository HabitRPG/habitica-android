package com.habitrpg.wearos.habitica.models.tasks

import com.squareup.moshi.Json
import java.util.Date

open class TaskGroupPlan {

    @Json(name="id")
    var groupID: String? = null
    var managerNotes: String? = null
    var sharedCompletion: String? = null
    var assignedDate: Date? = null
    var assigningUsername: String? = null
    var assignedUsers: List<String> = listOf()

    var approvalRequested: Boolean = false
    var approvalApproved: Boolean = false
    var approvalRequired: Boolean = false
}
