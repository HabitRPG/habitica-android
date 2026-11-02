@file:UseSerializers(DateSerializer::class, RealmListSerializer::class)

package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.AssignedDetailsListSerializer
import com.habitrpg.android.habitica.utils.RealmListSerializer
import com.habitrpg.shared.habitica.serialization.DateSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@RealmClass(embedded = true)
@Serializable
open class GroupAssignedDetails :
    RealmObject(),
    BaseObject {
    var assignedDate: Date? = null
    var assignedUsername: String? = null
    var assignedUserID: String? = null
    var assigningUsername: String? = null
    var completed: Boolean = false
    var completedDate: Date? = null
}

@RealmClass(embedded = true)
@Serializable
open class TaskGroupPlan :
    RealmObject(),
    BaseObject {
    fun assignedDetailsFor(userID: String): GroupAssignedDetails? = assignedUsersDetail.firstOrNull { it.assignedUserID == userID }

    @SerialName("id")
    var groupID: String? = null
    var managerNotes: String? = null
    var sharedCompletion: String? = null
    var assignedDate: Date? = null
    var assigningUsername: String? = null
    var assignedUsers: RealmList<String> = RealmList()

    @Serializable(with = AssignedDetailsListSerializer::class)
    var assignedUsersDetail: RealmList<GroupAssignedDetails> = RealmList()

    var approvalRequested: Boolean? = false
    var approvalApproved: Boolean? = false
    var approvalRequired: Boolean? = false
}
