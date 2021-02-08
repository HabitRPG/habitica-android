package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm
import io.realm.RealmResults


class RealmTagLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TagLocalRepository {
    override fun deleteTag(tagID: String) {
        val tag = realm.where(Tag::class.java).equalTo("id", tagID).findFirst()
        executeTransaction { tag?.deleteFromRealm() }
    }

    override fun getTags(userId: String): Flowable<RealmResults<Tag>> {
        return RxJavaBridge.toV3Flowable(realm.where(Tag::class.java).equalTo("userId", userId).findAll().asFlowable())
    }

    override fun removeOldTags(onlineTags: List<Tag>, userID: String) {
        val localTags = realm.where(Tag::class.java).equalTo("userId", userID).findAll().createSnapshot()
        for (localTag in localTags) {
            if (!onlineTags.contains(localTag)) {
                localTag.deleteFromRealm()
            }
        }
    }
}
