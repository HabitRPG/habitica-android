package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate

actual open class TutorialStep {
    actual var key: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var tutorialGroup: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var identifier: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var wasCompleted: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var displayedOn: NativeDate?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual fun shouldDisplay(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}