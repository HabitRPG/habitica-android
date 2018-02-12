package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState

import java.util.Date

import rx.Observable
import rx.functions.Action1

abstract class ContentRepositoryImpl<T : ContentLocalRepository>(localRepository: T, apiClient: ApiClient) : BaseRepositoryImpl<T>(localRepository, apiClient), ContentRepository {

    private var lastContentSync = 0L
    private var lastWorldStateSync = 0L

    override fun retrieveContent(): Observable<ContentResult> {
        return retrieveContent(false)
    }

    override fun retrieveContent(forced: Boolean): Observable<ContentResult> {
        val now = Date().time
        return if (forced || now - this.lastContentSync > 3600000) {
            lastContentSync = now
            apiClient.getContent().doOnNext({ localRepository.saveContent(it) })
        } else {
            Observable.just(null)
        }
    }

    override fun retrieveWorldState(): Observable<WorldState> {
        val now = Date().time
        return if (now - this.lastWorldStateSync > 3600000) {
            lastWorldStateSync = now
            apiClient.getWorldState().doOnNext({ localRepository.saveWorldState(it) })
        } else {
            Observable.just(null)
        }
    }
}
