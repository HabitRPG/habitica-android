package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.Tag

import io.reactivex.rxjava3.core.Flowable

interface TagLocalRepository : BaseLocalRepository {
    fun getTags(userId: String): Flowable<out List<Tag>>

    fun removeOldTags(onlineTags: List<Tag>, userID: String)
    fun deleteTag(tagID: String)
}
