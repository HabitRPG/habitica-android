package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import kotlinx.coroutines.flow.Flow

class TagRepositoryImpl(localRepository: TagLocalRepository, apiClient: ApiClient, userID: String) : BaseRepositoryImpl<TagLocalRepository>(localRepository, apiClient, userID), TagRepository {

    override fun getTags(): Flow<List<Tag>> {
        return getTags(userID)
    }

    override fun getTags(userId: String): Flow<List<Tag>> {
        return localRepository.getTags(userId)
    }

    override suspend fun createTag(tag: Tag): Tag? {
        val savedTag = apiClient.createTag(tag) ?: return null
        savedTag.userId = userID
        localRepository.save(savedTag)
        return savedTag
    }

    override suspend fun updateTag(tag: Tag): Tag? {
        val savedTag = apiClient.updateTag(tag.id, tag) ?: return null
        savedTag.userId = userID
        localRepository.save(savedTag)
        return savedTag
    }

    override suspend fun deleteTag(id: String): Void? {
        apiClient.deleteTag(id)
        localRepository.deleteTag(id)
        return null
    }

    override suspend fun createTags(tags: Collection<Tag>): List<Tag> {
        return tags.mapNotNull {
            createTag(it)
        }
    }

    override suspend fun updateTags(tags: Collection<Tag>): List<Tag> {
        return tags.mapNotNull {
            updateTag(it)
        }
    }

    override suspend fun deleteTags(tagIds: Collection<String>): List<Void> {
        return tagIds.mapNotNull {
            deleteTag(it)
        }
    }
}
