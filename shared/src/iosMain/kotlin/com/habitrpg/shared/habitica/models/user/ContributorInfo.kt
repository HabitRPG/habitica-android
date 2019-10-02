package com.habitrpg.shared.habitica.models.user

actual open class ContributorInfo {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var user: User?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var admin: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var contributions: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var level: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var text: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual val contributorColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val contributorForegroundColor: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun getAdmin(): Boolean? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setAdmin(admin: Boolean?) {
    }

    actual companion object {
        actual val CONTRIBUTOR_COLOR_DICT: SparseIntArray
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    }

}