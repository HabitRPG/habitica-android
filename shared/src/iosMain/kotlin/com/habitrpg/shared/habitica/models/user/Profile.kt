package com.habitrpg.shared.habitica.models.user

actual open class Profile {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    internal actual var user: User?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var name: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var blurb: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var imageUrl: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual constructor(name: String, blurb: String, imageUrl: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}