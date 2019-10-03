package com.habitrpg.shared.habitica.models.social

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class ChallengeMembership : RealmObject {
    @PrimaryKey
    actual var combinedID: String = ""

    actual var userID: String = ""
        set(value) {
            field = value
            combinedID = userID + challengeID
        }
    actual var challengeID: String = ""
        set(value) {
            field = value
            combinedID = userID + challengeID
        }

    actual constructor(userID: String, challengeID: String) : super() {
        this.userID = userID
        this.challengeID = challengeID
    }

    actual constructor() : super()

    actual override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == ChallengeMembership::class.java) {
            this.combinedID == (other as ChallengeMembership).combinedID
        } else super.equals(other)
    }

    actual override fun hashCode(): Int {
        var result = combinedID.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + challengeID.hashCode()
        return result
    }
}