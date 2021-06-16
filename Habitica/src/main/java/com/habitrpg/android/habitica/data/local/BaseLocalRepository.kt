package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.Realm
import io.realm.RealmModel

interface BaseLocalRepository {

    val isClosed: Boolean
    var realm: Realm

    fun close()

    fun executeTransaction(transaction: (Realm) -> Unit)
    fun executeTransaction(transaction: Realm.Transaction)
    fun executeTransactionAsync(transaction: (Realm) -> Unit)
    fun executeTransactionAsync(transaction: Realm.Transaction)
    fun <T: BaseMainObject> modify(obj: T, transaction: (T) -> Unit)
    fun <T: BaseMainObject> modifyWithRealm(obj: T, transaction: (Realm, T) -> Unit)
    fun <T: BaseObject> getLiveObject(obj: T): T?

    fun <T: BaseObject> getUnmanagedCopy(managedObject: T): T
    fun <T: BaseObject> getUnmanagedCopy(list: List<T>): List<T>

    fun <T: BaseObject> save(objects: List<T>)
    fun <T: BaseObject> save(`object`: T)
    fun <T: BaseObject> saveSyncronous(`object`: T)
}
