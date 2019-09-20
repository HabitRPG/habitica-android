package com.habitrpg.shared.habitica.models.user

expect class Buffs {
    var userId: String?

    var con: Float?
    var str: Float?
    var per: Float?
    var _int: Float?

    var seafoam: Boolean?
    var snowball: Boolean?
    var spookySparkles: Boolean?
    var shinySeed: Boolean?
    var streaks: Boolean?

    fun merge(stats: Buffs?)

    constructor()
    constructor(snowball: Boolean, streaks: Boolean)
}