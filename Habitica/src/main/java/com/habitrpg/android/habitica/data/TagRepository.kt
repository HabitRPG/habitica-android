package com.habitrpg.android.habitica.data

import com.habitrpg.shared.habitica.models.Tag

import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.RealmResults

interface TagRepository : BaseRepository {

    fun getTags(): Flowable<RealmResults<Tag>>
    fun getTags(userId: String): Flowable<RealmResults<Tag>>

    fun createTag(tag: Tag): Flowable<Tag>
    fun updateTag(tag: Tag): Flowable<Tag>
    fun deleteTag(id: String): Flowable<Unit>


    fun createTags(tags: Collection<Tag>): Single<List<Tag>>
    fun updateTags(tags: Collection<Tag>): Single<List<Tag>>
    fun deleteTags(tagIds: Collection<String>): Single<List<Unit>>

    fun removeOldTags(onlineTags: List<Tag>, userID: String)
}
