package com.habitrpg.android.habitica.data.local

import android.renderscript.BaseObj
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject

interface BaseLocalRepository {

    val isClosed: Boolean
    var realm: Realm

    fun close()

    fun executeTransaction(transaction: (Realm) -> Unit)
    fun executeTransaction(transaction: Realm.Transaction)
    fun executeTransactionAsync(transaction: (Realm) -> Unit)
    fun executeTransactionAsync(transaction: Realm.Transaction)
    fun <T: BaseObject> modify(obj: T, transaction: (T) -> Unit)
    fun <T: BaseObject> modifyWithRealm(obj: T, transaction: (Realm, T) -> Unit)
    fun <T: RealmModel> getLiveObject(obj: T): T?

    fun <T: RealmObject> getUnmanagedCopy(managedObject: T): T
    fun <T: RealmObject> getUnmanagedCopy(list: List<T>): List<T>

    fun <T: RealmObject> save(objects: List<T>)
    fun <T: RealmObject> save(`object`: T)
    fun <T: RealmObject> saveSyncronous(`object`: T)
}
