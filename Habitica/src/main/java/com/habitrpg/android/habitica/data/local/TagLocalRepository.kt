package com.habitrpg.android.habitica.data.local

import com.habitrpg.shared.habitica.models.Tag

import io.reactivex.Flowable
import io.realm.RealmResults

interface TagLocalRepository : BaseLocalRepository {
    fun getTags(userId: String): Flowable<RealmResults<Tag>>

    fun removeOldTags(onlineTags: List<Tag>, userID: String)
    fun deleteTag(tagID: String)
}
