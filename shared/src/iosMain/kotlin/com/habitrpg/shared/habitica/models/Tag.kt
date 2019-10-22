package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.nativeLibraries.NativeList

actual open class Tag {
    actual var id: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var tasks: NativeList<Task>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var name: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    internal actual var challenge: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual fun getTasks(): List<Task>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun equals(o: Any?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}