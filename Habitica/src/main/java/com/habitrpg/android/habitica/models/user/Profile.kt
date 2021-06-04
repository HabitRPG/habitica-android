package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Profile : RealmObject, BaseObject {
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
