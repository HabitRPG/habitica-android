package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState

interface ContentLocalRepository : BaseLocalRepository {
    fun saveContent(contentResult: ContentResult)
    fun saveWorldState(worldState: WorldState)
}
