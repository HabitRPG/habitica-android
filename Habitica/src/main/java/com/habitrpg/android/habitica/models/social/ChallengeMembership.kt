package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class ChallengeMembership : RealmObject, BaseObject {
    @PrimaryKey
    var combinedID: String = ""

    var userID: String = ""
        set(value) {
            field = value
            combinedID = userID + challengeID
        }
    var challengeID: String = ""
        set(value) {
            field = value
            combinedID = userID + challengeID
        }

    constructor(userID: String, challengeID: String) : super() {
        this.userID = userID
        this.challengeID = challengeID
    }

    constructor() : super()

    override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == ChallengeMembership::class.java) {
            this.combinedID == (other as ChallengeMembership).combinedID
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = combinedID.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + challengeID.hashCode()
        return result
    }
}