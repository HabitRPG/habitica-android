package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import io.realm.Realm
import kotlinx.coroutines.flow.Flow

class RealmTagLocalRepository(
    realm: Realm,
) : RealmBaseLocalRepository(realm),
    TagLocalRepository {
    override fun deleteTag(tagID: String) {
        val tags = safeQuery { it.where(Tag::class.java).equalTo("id", tagID) }?.findAll() ?: return
        executeTransaction { tags.deleteAllFromRealm() }
    }

    override fun getTags(userId: String): Flow<List<Tag>> = safeFindAll {
        it.where(Tag::class.java).equalTo("userId", userId)
    }
}
