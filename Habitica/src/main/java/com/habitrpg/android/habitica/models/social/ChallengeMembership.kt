package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
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

    override fun equals(other: Any?): Boolean {
        return if (other is ChallengeMembership) {
            this.userID == other.userID && challengeID == other.challengeID
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = userID.hashCode()
        result = 31 * result + challengeID.hashCode()
        return result
    }
}