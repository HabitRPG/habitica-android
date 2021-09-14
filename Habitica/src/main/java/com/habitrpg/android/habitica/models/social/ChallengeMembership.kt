package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class ChallengeMembership : RealmObject, BaseObject {
    var userID: String = ""
    var challengeID: String = ""

    constructor(userID: String, challengeID: String) : super() {
        this.userID = userID
        this.challengeID = challengeID
    }

    constructor() : super()
}
