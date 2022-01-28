package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import io.reactivex.rxjava3.core.Flowable

interface ContentRepository: BaseRepository {
    fun retrieveContent(forced: Boolean = false): Flowable<ContentResult>

    fun retrieveWorldState(): Flowable<WorldState>
    fun getWorldState(): Flowable<WorldState>
}
