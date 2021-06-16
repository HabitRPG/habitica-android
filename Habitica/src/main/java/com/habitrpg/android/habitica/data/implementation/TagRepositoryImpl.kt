package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single


class TagRepositoryImpl(localRepository: TagLocalRepository, apiClient: ApiClient, userID: String) : BaseRepositoryImpl<TagLocalRepository>(localRepository, apiClient, userID), TagRepository {

    override fun getTags(): Flowable<out List<Tag>> {
        return getTags(userID)
    }

    override fun getTags(userId: String): Flowable<out List<Tag>> {
        return localRepository.getTags(userId)
    }

    override fun createTag(tag: Tag): Flowable<Tag> {
        return apiClient.createTag(tag)
                .doOnNext {
                    it.userId = userID
                    localRepository.save(it)
                }
    }

    override fun updateTag(tag: Tag): Flowable<Tag> {
        return apiClient.updateTag(tag.id, tag)
                .doOnNext {
                    it.userId = userID
                    localRepository.save(it)
                }
    }

    override fun deleteTag(id: String): Flowable<Void> {
        return apiClient.deleteTag(id)
                .doOnNext {
                    localRepository.deleteTag(id)
                }
    }

    override fun createTags(tags: Collection<Tag>): Single<List<Tag>> {
        return Flowable.defer { Flowable.fromIterable(tags) }
                .filter { tag -> tag.name.isNotEmpty() }
                .flatMap { this.createTag(it) }
                .toList()
    }

    override fun updateTags(tags: Collection<Tag>): Single<List<Tag>> {
        return Flowable.defer { Flowable.fromIterable(tags) }
                .flatMap { this.updateTag(it) }
                .toList()
    }

    override fun deleteTags(tagIds: Collection<String>): Single<List<Void>> {
        return Flowable.defer { Flowable.fromIterable(tagIds) }
                .flatMap { this.deleteTag(it) }
        .toList()
    }
}
