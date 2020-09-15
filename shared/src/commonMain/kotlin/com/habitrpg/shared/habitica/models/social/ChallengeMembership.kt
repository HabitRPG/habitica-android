package com.habitrpg.shared.habitica.models.social

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class ChallengeMembership : NativeRealmObject {
    @PrimaryKeyAnnotation
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
        return if (other is ChallengeMembership) {
            this.combinedID == other.combinedID
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = combinedID.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + challengeID.hashCode()
        return result
    }
}