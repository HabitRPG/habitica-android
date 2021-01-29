package com.habitrpg.android.habitica.models.tasks


import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class TaskGroupPlan : RealmObject() {

    @PrimaryKey
    internal var taskID: String? = null

    @SerializedName("id")
    var groupID: String? = null
    var managerNotes: String? = null
    var sharedCompletion: String? = null
    var assignedDate: Date? = null
    var assigningUsername: String? = null
    var assignedUsers: RealmList<String> = RealmList()

    var approvalRequested: Boolean = false
    var approvalApproved: Boolean = false
    var approvalRequired: Boolean = false
}
