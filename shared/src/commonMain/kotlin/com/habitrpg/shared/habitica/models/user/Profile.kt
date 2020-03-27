package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import kotlin.jvm.JvmOverloads

open class Profile : NativeRealmObject {

    @PrimaryKeyAnnotation
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
