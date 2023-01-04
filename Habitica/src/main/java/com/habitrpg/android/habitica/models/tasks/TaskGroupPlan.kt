package com.habitrpg.android.habitica.models.tasks

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date

@RealmClass(embedded = true)
open class GroupAssignedDetails: RealmObject(), BaseObject {
    var assignedDate: Date? = null
    var assignedUsername: String? = null
    var assignedUserID: String? = null
    var assigningUsername: String? = null
    var completed: Boolean = false
    var completedDate: Date? = null
}

@RealmClass(embedded = true)
open class TaskGroupPlan : RealmObject(), BaseObject {
    fun assignedDetailsFor(userID: String): GroupAssignedDetails? {
        return assignedUsersDetail.firstOrNull { it.assignedUserID == userID }
    }

    @SerializedName("id")
    var groupID: String? = null
    var managerNotes: String? = null
    var sharedCompletion: String? = null
    var assignedDate: Date? = null
    var assigningUsername: String? = null
    var assignedUsers: RealmList<String> = RealmList()
    var assignedUsersDetail: RealmList<GroupAssignedDetails> = RealmList()

    var approvalRequested: Boolean? = false
    var approvalApproved: Boolean? = false
    var approvalRequired: Boolean? = false
}
