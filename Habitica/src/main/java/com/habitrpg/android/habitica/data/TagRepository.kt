package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.Tag

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface TagRepository : BaseRepository {

    fun getTags(): Flowable<out List<Tag>>
    fun getTags(userId: String): Flowable<out List<Tag>>

    fun createTag(tag: Tag): Flowable<Tag>
    fun updateTag(tag: Tag): Flowable<Tag>
    fun deleteTag(id: String): Flowable<Void>


    fun createTags(tags: Collection<Tag>): Single<List<Tag>>
    fun updateTags(tags: Collection<Tag>): Single<List<Tag>>
    fun deleteTags(tagIds: Collection<String>): Single<List<Void>>
}
