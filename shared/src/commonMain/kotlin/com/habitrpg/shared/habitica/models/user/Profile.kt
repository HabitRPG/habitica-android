package com.habitrpg.shared.habitica.models.user

import kotlin.jvm.JvmOverloads

expect open class Profile {

    var userId: String?

    internal var user: User?
    var name: String?
    var blurb: String?
    var imageUrl: String?

    @JvmOverloads
    constructor(name: String, blurb: String = "", imageUrl: String = "")

    constructor()
}
