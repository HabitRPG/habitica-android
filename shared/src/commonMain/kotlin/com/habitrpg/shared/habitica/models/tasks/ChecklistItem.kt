package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.nativeLibraries.NativeParcel
import kotlin.jvm.JvmOverloads

expect class ChecklistItem {
    var id: String?
    var text: String?
    var completed: Boolean
    var position: Int

    @JvmOverloads
    constructor(id: String? = null, text: String? = null, completed: Boolean = false)

    constructor(item: ChecklistItem)

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    fun describeContents(): Int

    fun writeToParcel(dest: NativeParcel, flags: Int)

    companion object CREATOR

    constructor(source: NativeParcel)
}