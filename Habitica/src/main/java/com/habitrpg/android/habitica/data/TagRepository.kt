package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository : BaseRepository {
    fun getTags(): Flow<List<Tag>>

    fun getTags(userId: String): Flow<List<Tag>>

    suspend fun createTag(tag: Tag): Tag?

    suspend fun updateTag(tag: Tag): Tag?

    suspend fun deleteTag(id: String): Void?

    suspend fun createTags(tags: Collection<Tag>): List<Tag>

    suspend fun updateTags(tags: Collection<Tag>): List<Tag>

    suspend fun deleteTags(tagIds: Collection<String>): List<Void>
}
