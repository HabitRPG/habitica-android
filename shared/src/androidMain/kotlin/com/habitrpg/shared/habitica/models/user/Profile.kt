package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Profile : RealmObject {

    @PrimaryKey
    actual var userId: String? = null

    internal actual var user: User? = null
    actual var name: String? = null
    actual var blurb: String? = null
    actual var imageUrl: String? = null

    @JvmOverloads
    actual constructor(name: String, blurb: String, imageUrl: String) {
        this.name = name
        this.blurb = blurb
        this.imageUrl = imageUrl
    }

    actual constructor()
}
