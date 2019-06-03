package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState

import io.reactivex.Flowable

interface ContentRepository {

    fun retrieveContent(): Flowable<ContentResult>
    fun retrieveContent(forced: Boolean): Flowable<ContentResult>

    fun retrieveWorldState(): Flowable<WorldState>
}
