package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Profile : RealmObject {

    @PrimaryKey
    var userId: String? = null

    internal var user: User? = null
    var name: String? = null
    var blurb: String? = null
    var imageUrl: String? = null

    @JvmOverloads
    constructor(name: String, blurb: String = "", imageUrl: String = "") {
        this.name = name
        this.blurb = blurb
        this.imageUrl = imageUrl
    }

    constructor()
}
