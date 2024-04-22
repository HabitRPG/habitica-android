package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.BaseObject

interface BaseRepository {
    val isClosed: Boolean

    fun close()

    fun <T : BaseObject> getUnmanagedCopy(obj: T): T

    fun <T : BaseObject> getUnmanagedCopy(list: List<T>): List<T>
}
