package com.habitrpg.android.habitica.models.social

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class GroupMembership : RealmObject {
    @PrimaryKey
    var combinedID: String = ""

    var userID: String = ""
        set(value) {
            field = value
            combinedID = userID + groupID
        }
    var groupID: String = ""
        set(value) {
            field = value
            combinedID = userID + groupID
        }

    constructor(userID: String, groupID: String) : super() {
        this.userID = userID
        this.groupID = groupID
    }

    constructor() : super()
}
