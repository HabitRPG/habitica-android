package com.habitrpg.shared.habitica.models.social


expect open class ChallengeMembership {
    var combinedID: String

    var userID: String
    var challengeID: String

    constructor(userID: String, challengeID: String)

    constructor()

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}