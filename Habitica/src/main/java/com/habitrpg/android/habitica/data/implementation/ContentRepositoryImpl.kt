package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState

import java.util.Date

import rx.Observable
import rx.functions.Action1

internal abstract class ContentRepositoryImpl<T : ContentLocalRepository>(localRepository: T, apiClient: ApiClient) : BaseRepositoryImpl<T>(localRepository, apiClient), ContentRepository {

    private var lastContentSync = Date()
    private var lastWorldStateSync = Date()

    override fun retrieveContent(): Observable<ContentResult> {
        return retrieveContent(false)
    }

    override fun retrieveContent(forced: Boolean): Observable<ContentResult> {
        return if (forced || Date().time - this.lastContentSync.time > 3600000) {
            lastContentSync = Date()
            apiClient.content.doOnNext({ localRepository.saveContent(it) })
        } else {
            Observable.just(null)
        }
    }

    override fun retrieveWorldState(): Observable<WorldState> {
        return if (Date().time - this.lastWorldStateSync.time > 3600000) {
            lastWorldStateSync = Date()
            apiClient.worldState.doOnNext({ localRepository.saveWorldState(it) })
        } else {
            Observable.just(null)
        }
    }
}
