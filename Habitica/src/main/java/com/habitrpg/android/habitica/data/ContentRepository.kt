package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import io.reactivex.rxjava3.core.Flowable

interface ContentRepository: BaseRepository {
    suspend fun retrieveContent(forced: Boolean = false): ContentResult?

    suspend fun retrieveWorldState(): WorldState?
    fun getWorldState(): Flowable<WorldState>
}
