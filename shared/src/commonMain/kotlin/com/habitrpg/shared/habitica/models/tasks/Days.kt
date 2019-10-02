package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.nativeLibraries.NativeParcel

expect open class Days {
    var taskId: String?
    var m: Boolean
    var t: Boolean
    var w: Boolean
    var th: Boolean
    var f: Boolean
    var s: Boolean
    var su: Boolean

    fun getForDay(day: Int): Boolean

    fun describeContents(): Int

    fun writeToParcel(dest: NativeParcel, flags: Int)

    protected constructor(`in`: NativeParcel)
}
