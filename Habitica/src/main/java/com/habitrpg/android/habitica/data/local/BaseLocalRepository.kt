package com.habitrpg.android.habitica.data.local

import io.realm.Realm
import io.realm.RealmObject

interface BaseLocalRepository {

    val isClosed: Boolean

    fun close()

    fun executeTransaction(transaction: (Realm) -> Unit)
    fun executeTransaction(transaction: Realm.Transaction)

    fun <T : RealmObject> getUnmanagedCopy(managedObject: T): T
    fun <T : RealmObject> getUnmanagedCopy(list: List<T>): List<T>

    fun <T : RealmObject> save(objects: List<T>)
    fun <T : RealmObject> save(`object`: T)
    fun <T : RealmObject> saveSyncronous(`object`: T)
}
