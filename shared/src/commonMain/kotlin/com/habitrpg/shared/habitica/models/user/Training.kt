package com.habitrpg.shared.habitica.models.user

expect class Training {
    var userId: String?

    internal var stats: Stats?
    var con: Float?
    var str: Float?
    var per: Float?
    var _int: Float?

    fun merge(stats: Training?)
}
