package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.BaseLocalRepository

import io.realm.Realm
import io.realm.RealmObject

abstract class RealmBaseLocalRepository internal constructor(protected var realm: Realm) : BaseLocalRepository {

    override val isClosed: Boolean
        get() = realm.isClosed

    override fun close() {
        realm.close()
    }

    override fun executeTransaction(transaction: (Realm) -> Unit) {
        realm.executeTransaction {
            transaction(it)
        }
    }

    override fun executeTransaction(transaction: Realm.Transaction) {
        realm.executeTransaction(transaction)
    }

    override fun <T : RealmObject> getUnmanagedCopy(managedObject: T): T {
        return if (managedObject.isManaged && managedObject.isValid) {
            realm.copyFromRealm(managedObject)
        } else {
            managedObject
        }
    }

    override fun <T : RealmObject> getUnmanagedCopy(list: List<T>): List<T> {
        return realm.copyFromRealm(list)
    }

    override fun <T : RealmObject> save(`object`: T) {
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(`object`) }
    }

    override fun <T : RealmObject> save(objects: List<T>) {
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(objects) }
    }

    override fun <T : RealmObject> saveSyncronous(`object`: T) {
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(`object`) }
    }
}
