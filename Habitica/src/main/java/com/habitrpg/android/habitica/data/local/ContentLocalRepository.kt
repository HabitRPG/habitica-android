package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import io.reactivex.rxjava3.core.Flowable

interface ContentLocalRepository : BaseLocalRepository {
    fun saveContent(contentResult: ContentResult)
    fun saveWorldState(worldState: WorldState)
    fun getWorldState(): Flowable<WorldState>
}
