package com.habitrpg.shared.habitica.models.user

expect open class Hair {
    var userId: String?

    var preferences: Preferences?
    var mustache: Int
    var beard: Int
    var bangs: Int
    var base: Int
    var flower: Int
    var color: String?

    constructor()

    constructor(mustache: Int, beard: Int, bangs: Int, base: Int, color: String, flower: Int)

    fun isAvailable(hairId: Int): Boolean
}
