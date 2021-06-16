package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm


class RealmTagLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TagLocalRepository {
    override fun deleteTag(tagID: String) {
        val tag = realm.where(Tag::class.java).equalTo("id", tagID).findFirst()
        executeTransaction { tag?.deleteFromRealm() }
    }

    override fun getTags(userId: String): Flowable<out List<Tag>> {
        return RxJavaBridge.toV3Flowable(realm.where(Tag::class.java).equalTo("userId", userId).findAll().asFlowable())
    }
}
