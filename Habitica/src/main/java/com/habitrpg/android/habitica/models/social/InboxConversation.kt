package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.user.ContributorInfo
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

open class InboxConversation : RealmObject(), BaseObject {
    @PrimaryKey
    var combinedID: String = ""
    var uuid: String = ""
        set(value) {
            field = value
            combinedID = userID + value
        }
    var userID: String = ""
        set(value) {
            field = value
            combinedID = value + uuid
        }
    var username: String? = null
    var user: String? = null
    var timestamp: Date? = null
    var contributor: ContributorInfo? = null
    var userStyles: UserStyles? = null
    var text: String? = null

    val formattedUsername: String?
        get() = if (username?.isNotEmpty() == true) "@$username" else null
}
