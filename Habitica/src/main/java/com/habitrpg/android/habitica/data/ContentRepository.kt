package com.habitrpg.android.habitica.data

import android.content.Context
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState

import io.reactivex.Flowable

interface ContentRepository {

    fun retrieveContent(context: Context?): Flowable<ContentResult>
    fun retrieveContent(context: Context?, forced: Boolean): Flowable<ContentResult>

    fun retrieveWorldState(): Flowable<WorldState>
}
