package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Hair : NativeRealmObject {

    @PrimaryKeyAnnotation
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
