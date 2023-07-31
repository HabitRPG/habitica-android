@file:OptIn(ExperimentalCoroutinesApi::class)

package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class TagRepositoryImpl(
    localRepository: TagLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler
) : BaseRepositoryImpl<TagLocalRepository>(localRepository, apiClient, authenticationHandler),
    TagRepository {

    override fun getTags() = authenticationHandler.userIDFlow.flatMapLatest { getTags(it) }

    override fun getTags(userId: String): Flow<List<Tag>> {
        return localRepository.getTags(userId)
    }

    override suspend fun createTag(tag: Tag): Tag? {
        val savedTag = apiClient.createTag(tag) ?: return null
        savedTag.userId = currentUserID
        localRepository.save(savedTag)
        return savedTag
    }

    override suspend fun updateTag(tag: Tag): Tag? {
        val savedTag = apiClient.updateTag(tag.id, tag) ?: return null
        savedTag.userId = currentUserID
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
