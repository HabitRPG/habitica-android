package com.habitrpg.android.habitica.data

import com.google.gson.JsonObject
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import kotlinx.coroutines.flow.Flow

interface ContentRepository : BaseRepository {
    suspend fun retrieveContent(forced: Boolean = false): ContentResult?

    suspend fun retrieveWorldState(forced: Boolean = false): WorldState?

    fun getWorldState(): Flow<WorldState>

    suspend fun retrieveClientConfig(): JsonObject?
}
