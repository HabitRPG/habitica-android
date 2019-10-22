package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.nativeLibraries.NativeParcel

actual class ChecklistItem {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var text: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var completed: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var position: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual constructor(id: String?, text: String?, completed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor(item: ChecklistItem) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun equals(other: Any?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun hashCode(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun writeToParcel(dest: NativeParcel, flags: Int) {
    }

    actual companion object CREATOR

}