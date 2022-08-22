package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarHair
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Hair : RealmObject, BaseObject, AvatarHair {
    final override var mustache: Int = 0
    final override var beard: Int = 0
    final override var bangs: Int = 0
    final override var base: Int = 0
    final override var flower: Int = 0
    final override var color: String? = null

    constructor()

    constructor(mustache: Int, beard: Int, bangs: Int, base: Int, color: String, flower: Int) {
        this.mustache = mustache
        this.beard = beard
        this.bangs = bangs
        this.base = base
        this.color = color
        this.flower = flower
    }
}
