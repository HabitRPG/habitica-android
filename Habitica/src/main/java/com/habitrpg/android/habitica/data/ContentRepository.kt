package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import kotlinx.coroutines.flow.Flow

interface ContentRepository: BaseRepository {
    suspend fun retrieveContent(forced: Boolean = false): ContentResult?

    suspend fun retrieveWorldState(): WorldState?
    fun getWorldState(): Flow<WorldState>
}
