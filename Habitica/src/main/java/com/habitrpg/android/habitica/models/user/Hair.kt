package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Hair : RealmObject {

    @PrimaryKey
    var userId: String? = null

    var preferences: Preferences? = null
    var mustache: Int = 0
    var beard: Int = 0
    var bangs: Int = 0
    var base: Int = 0
    var flower: Int = 0
    var color: String? = null

    constructor()

    constructor(mustache: Int, beard: Int, bangs: Int, base: Int, color: String, flower: Int) {
        this.mustache = mustache
        this.beard = beard
        this.bangs = bangs
        this.base = base
        this.color = color
        this.flower = flower
    }

    fun isAvailable(hairId: Int): Boolean {
        return hairId > 0
    }
}
