package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Hair : RealmObject {

    @PrimaryKey
    actual var userId: String? = null

    actual var preferences: Preferences? = null
    actual var mustache: Int = 0
    actual var beard: Int = 0
    actual var bangs: Int = 0
    actual var base: Int = 0
    actual var flower: Int = 0
    actual var color: String? = null

    actual constructor()

    actual constructor(mustache: Int, beard: Int, bangs: Int, base: Int, color: String, flower: Int) {
        this.mustache = mustache
        this.beard = beard
        this.bangs = bangs
        this.base = base
        this.color = color
        this.flower = flower
    }

    actual fun isAvailable(hairId: Int): Boolean {
        return hairId > 0
    }
}
