package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.BaseRepository
import com.habitrpg.android.habitica.data.local.BaseLocalRepository
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.modules.AuthenticationHandler

abstract class BaseRepositoryImpl<T : BaseLocalRepository>(
    protected val localRepository: T,
    protected val apiClient: ApiClient,
    protected val authenticationHandler: AuthenticationHandler
) : BaseRepository {

    val currentUserID: String
        get() = authenticationHandler.currentUserID ?: ""

    override fun close() {
        this.localRepository.close()
    }

    override fun <T : BaseObject> getUnmanagedCopy(list: List<T>): List<T> {
        return localRepository.getUnmanagedCopy(list)
    }

    override val isClosed: Boolean
        get() = localRepository.isClosed

    override fun <T : BaseObject> getUnmanagedCopy(obj: T): T {
        return localRepository.getUnmanagedCopy(obj)
    }
}
