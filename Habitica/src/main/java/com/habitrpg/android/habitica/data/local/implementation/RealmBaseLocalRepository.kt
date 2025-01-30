package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.BaseLocalRepository
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.kotlin.deleteFromRealm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean

abstract class RealmBaseLocalRepository internal constructor(override var realm: Realm) :
    BaseLocalRepository {
    override val isClosed: Boolean
        get() = realm.isClosed

    override fun close() {
        realm.close()
    }

    override fun executeTransaction(transaction: (Realm) -> Unit) {
        pendingSaves.add(transaction)
        if (isSaving.compareAndSet(false, true)) {
            process()
        }
    }

    override fun <T : BaseObject> getUnmanagedCopy(managedObject: T): T {
        return if (managedObject is RealmObject && managedObject.isManaged && managedObject.isValid) {
            realm.copyFromRealm(managedObject)
        } else {
            managedObject
        }
    }

    override fun <T : BaseObject> getUnmanagedCopy(list: List<T>): List<T> {
        if (isClosed) {
            return emptyList()
        }
        return realm.copyFromRealm(list)
    }

    companion object {
        private var isSaving = AtomicBoolean(false)
        private var pendingSaves = mutableListOf<Any>()
    }

    private fun <T : RealmModel> copy(
        realm: Realm,
        obj: T
    ) {
        try {
            realm.insertOrUpdate(obj)
        } catch (_: java.lang.IllegalArgumentException) {
        }
    }

    private fun process() {
        if (isClosed) {
            return
        }
        realm.executeTransaction {
            while (pendingSaves.isNotEmpty()) {
                val pending = pendingSaves.removeAt(0)
                @Suppress("UNCHECKED_CAST")
                if (pending is RealmModel) {
                    copy(it, pending)
                } else if (pending as? List<BaseObject> != null) {
                    it.insertOrUpdate(pending)
                } else if (pending is Function0<*>) {
                    pending.invoke()
                } else if (pending as? Function1<Realm, *> != null) {
                    pending.invoke(it)
                }
            }
            isSaving.set(false)
        }
    }

    override fun <T : BaseObject> save(obj: T) {
        pendingSaves.add(obj)
        if (isSaving.compareAndSet(false, true)) {
            process()
        }
    }

    override fun <T : BaseObject> save(objects: List<T>) {
        pendingSaves.add(objects)
        if (isSaving.compareAndSet(false, true)) {
            process()
        }
    }

    override fun <T : BaseMainObject> modify(
        obj: T,
        transaction: (T) -> Unit
    ) {
        if (isClosed) {
            return
        }
        val liveObject = getLiveObject(obj) ?: return
        executeTransaction {
            transaction(liveObject)
        }
    }

    override fun <T : BaseMainObject> delete(obj: T) {
        if (isClosed) {
            return
        }
        val liveObject = getLiveObject(obj) ?: return
        executeTransaction {
            liveObject.deleteFromRealm()
        }
    }

    override fun getLiveUser(id: String): User? {
        return realm.where(User::class.java).equalTo("id", id).findFirst()
    }

    override fun <T : BaseObject> getLiveObject(obj: T): T? {
        if (isClosed) return null
        if (obj !is RealmObject || !obj.isManaged) return obj
        val baseObject = obj as? BaseMainObject ?: return null
        @Suppress("UNCHECKED_CAST")
        return realm.where(baseObject.realmClass)
            .equalTo(baseObject.primaryIdentifierName, baseObject.primaryIdentifier)
            .findFirst() as? T
    }

    fun queryUser(userID: String): Flow<User?> {
        return realm.where(User::class.java)
            .equalTo("id", userID)
            .findAll()
            .toFlow()
            .filter { it.isLoaded && it.isValid && !it.isEmpty() }
            .map { it.firstOrNull() }
    }
}
