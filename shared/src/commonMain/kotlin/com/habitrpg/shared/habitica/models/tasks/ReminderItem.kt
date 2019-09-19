package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate
import com.habitrpg.shared.habitica.nativeLibraries.NativeParcel

expect open class RemindersItem {
    var id: String?
    var startDate: NativeDate?
    var time: NativeDate?

    //Use to store task type before a task is created
    var type: String?

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    fun describeContents(): Int

    fun writeToParcel(dest: NativeParcel, flags: Int)

    companion object CREATOR {}

    constructor(source: NativeParcel)

    constructor()
}