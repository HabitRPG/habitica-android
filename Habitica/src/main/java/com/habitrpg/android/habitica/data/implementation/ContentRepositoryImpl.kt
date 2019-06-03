package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import io.reactivex.Flowable
import java.util.*

abstract class ContentRepositoryImpl<T : ContentLocalRepository>(localRepository: T, apiClient: ApiClient) : BaseRepositoryImpl<T>(localRepository, apiClient), ContentRepository {

    private var lastContentSync = 0L
    private var lastWorldStateSync = 0L

    override fun retrieveContent(): Flowable<ContentResult> {
        return retrieveContent(false)
    }

    override fun retrieveContent(forced: Boolean): Flowable<ContentResult> {
        val now = Date().time
        return if (forced || now - this.lastContentSync > 3) {
            lastContentSync = now
            apiClient.content.doOnNext { localRepository.saveContent(it) }
        } else {
            Flowable.empty()
        }
    }

    override fun retrieveWorldState(): Flowable<WorldState> {
        val now = Date().time
        return if (now - this.lastWorldStateSync > 3600000) {
            lastWorldStateSync = now
            apiClient.worldState.doOnNext { localRepository.saveWorldState(it) }
        } else {
            Flowable.empty()
        }
    }
}
