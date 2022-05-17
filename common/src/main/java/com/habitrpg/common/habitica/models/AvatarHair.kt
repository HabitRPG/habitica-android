package com.habitrpg.common.habitica.models

interface AvatarHair {
    var mustache: Int
    var beard: Int
    var bangs: Int
    var base: Int
    var flower: Int
    var color: String?

    fun isAvailable(hairId: Int): Boolean {
        return hairId > 0
    }
}
