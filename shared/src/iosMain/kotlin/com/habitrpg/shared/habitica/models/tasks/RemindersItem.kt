package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.NativeParcel

actual open class RemindersItem {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var startDate: NativeDate?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var time: NativeDate?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var type: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

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

    actual constructor() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor(source: NativeParcel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual companion object CREATOR {
        actual fun createFromParcel(source: NativeParcel): RemindersItem {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        actual fun newArray(size: Int): Array<RemindersItem?> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

}