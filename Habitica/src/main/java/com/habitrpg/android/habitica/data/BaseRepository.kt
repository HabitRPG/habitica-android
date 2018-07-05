package com.habitrpg.android.habitica.data

import io.realm.RealmObject

interface BaseRepository {

    val isClosed: Boolean

    fun close()

    fun <T : RealmObject> getUnmanagedCopy(obj: T): T
    fun <T : RealmObject> getUnmanagedCopy(list: List<T>): List<T>
}
