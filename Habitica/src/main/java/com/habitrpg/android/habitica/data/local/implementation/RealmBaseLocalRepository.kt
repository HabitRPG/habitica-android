package com.habitrpg.android.habitica.data.local.implementation

import android.renderscript.BaseObj
import com.habitrpg.android.habitica.data.local.BaseLocalRepository
import com.habitrpg.android.habitica.models.BaseObject

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.kotlin.isManaged
import io.realm.kotlin.where

abstract class RealmBaseLocalRepository internal constructor(override var realm: Realm) : BaseLocalRepository {

    override val isClosed: Boolean
        get() = realm.isClosed

    override fun close() {
        realm.close()
    }

    override fun executeTransaction(transaction: (Realm) -> Unit) {
        if (isClosed) { return }
        realm.executeTransaction {
            transaction(it)
        }
    }

    override fun executeTransaction(transaction: Realm.Transaction) {
        if (isClosed) { return }
        realm.executeTransaction(transaction)
    }

    override fun executeTransactionAsync(transaction: (Realm) -> Unit) {
        if (isClosed) { return }
        realm.executeTransactionAsync {
            transaction(it)
        }
    }

    override fun executeTransactionAsync(transaction: Realm.Transaction) {
        if (isClosed) { return }
        realm.executeTransactionAsync(transaction)
    }

    override fun <T : RealmObject> getUnmanagedCopy(managedObject: T): T {
        return if (managedObject.isManaged && managedObject.isValid) {
            realm.copyFromRealm(managedObject)
        } else {
            managedObject
        }
    }

    override fun <T : RealmObject> getUnmanagedCopy(list: List<T>): List<T> {
        if (isClosed) { return emptyList() }
        return realm.copyFromRealm(list)
    }

    override fun <T : RealmObject> save(`object`: T) {
        if (isClosed) { return }
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(`object`) }
    }

    override fun <T : RealmObject> save(objects: List<T>) {
        if (isClosed) { return }
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(objects) }
    }

    override fun <T : RealmObject> saveSyncronous(`object`: T) {
        if (isClosed) { return }
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(`object`) }
    }

    override fun <T: BaseObject> modify(obj: T, transaction: (T) -> Unit) {
        if (isClosed) { return }
        val liveObject =  getLiveObject(obj) ?: return
        realm.executeTransaction {
            transaction(liveObject)
        }
    }

    override fun <T: BaseObject> modifyWithRealm(obj: T, transaction: (Realm, T) -> Unit) {
        if (isClosed) { return }
        val liveObject = getLiveObject(obj) ?: return
        realm.executeTransaction {
            transaction(it, liveObject)
        }
    }

    override fun <T: RealmModel> getLiveObject(obj: T): T? {
        if (isClosed) return null
        if (!obj.isManaged()) return obj
        val baseObject = obj as? BaseObject ?: return null
        return realm.where(baseObject.realmClass).equalTo(baseObject.primaryIdentifierName, baseObject.primaryIdentifier).findFirst() as? T
    }
}
