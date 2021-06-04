package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class GroupMembership : RealmObject, BaseObject {
    var userID: String = ""
    var groupID: String = ""

    constructor(userID: String, groupID: String) : super() {
        this.userID = userID
        this.groupID = groupID
    }

    constructor() : super()
}
