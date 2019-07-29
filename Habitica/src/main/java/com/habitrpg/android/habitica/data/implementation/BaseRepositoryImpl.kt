package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.BaseRepository
import com.habitrpg.android.habitica.data.local.BaseLocalRepository

import io.realm.RealmObject

abstract class BaseRepositoryImpl<T : BaseLocalRepository>(protected val localRepository: T, protected val apiClient: ApiClient, protected val userID: String = "") : BaseRepository {

    override fun close() {
        this.localRepository.close()
    }

    override fun <T : RealmObject> getUnmanagedCopy(list: List<T>): List<T> {
        return localRepository.getUnmanagedCopy(list)
    }

    override val isClosed: Boolean
        get() = localRepository.isClosed

    override fun <T : RealmObject> getUnmanagedCopy(obj: T): T {
        return localRepository.getUnmanagedCopy(obj)
    }
}
