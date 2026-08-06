package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.BaseLocalRepository
import com.habitrpg.android.habitica.extensions.findAllAsFlow
import com.habitrpg.android.habitica.extensions.findOneAsFlow
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.kotlin.deleteFromRealm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.concurrent.atomic.AtomicBoolean

abstract class RealmBaseLocalRepository internal constructor(
    internal var realm: Realm,
) : BaseLocalRepository {
    override val isClosed: Boolean
        get() = realm.isClosed

    override fun refreshLocalData() {
        val r = realm
        if (r.isClosed) return
        try {
            r.refresh()
        } catch (_: IllegalStateException) {
        }
    }

    override fun close() {
        realm.close()
    }

    internal fun executeTransaction(transaction: (Realm) -> Unit) {
        pendingSaves.add(transaction)
        if (isSaving.compareAndSet(false, true)) {
            process()
        }
    }

    override fun <T : BaseObject> getUnmanagedCopy(managedObject: T): T =
        if (managedObject is RealmObject && managedObject.isManaged && managedObject.isValid) {
            realm.copyFromRealm(managedObject)
        } else {
            managedObject
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
        obj: T,
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
        transaction: (T) -> Unit,
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

    override fun getLiveUser(id: String): User? = realm.where(User::class.java).equalTo("id", id).findFirst()

    override fun <T : BaseObject> getLiveObject(obj: T): T? {
        if (isClosed) return null
        if (obj !is RealmObject || !obj.isManaged) return obj
        val baseObject = obj as? BaseMainObject ?: return null
        @Suppress("UNCHECKED_CAST")
        return realm
            .where(baseObject.realmClass)
            .equalTo(baseObject.primaryIdentifierName, baseObject.primaryIdentifier)
            .findFirst() as? T
    }

    fun queryUser(userID: String) = realm
            .where(User::class.java)
            .equalTo("id", userID)
            .findOneAsFlow()

    internal fun <T: RealmModel> safeQuery(queryBuilder: (Realm) -> RealmQuery<T>): RealmQuery<T>? =
        if (isClosed) {
            null
        } else {
            queryBuilder(realm)
        }


    internal fun <T: RealmModel> safeFindAll(queryBuilder: (Realm) -> RealmQuery<T>): Flow<List<T>> =
        safeQuery { queryBuilder(it) }?.findAllAsFlow() ?: emptyFlow()

    internal fun <T: RealmModel> safeFindOne(queryBuilder: (Realm) -> RealmQuery<T>): Flow<T> =
        safeQuery { queryBuilder(it) }?.findOneAsFlow() ?: emptyFlow()
}
