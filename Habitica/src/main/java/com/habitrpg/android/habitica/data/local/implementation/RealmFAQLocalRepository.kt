package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmResults

class RealmFAQLocalRepository(realm: Realm) : RealmContentLocalRepository(realm), FAQLocalRepository {
    override fun getArticle(position: Int): Flowable<FAQArticle> {
        return realm.where(FAQArticle::class.java)
                .equalTo("position", position)
                .findAll()
                .asFlowable()
                .filter{ it.isLoaded && it.count() > 0 }
                .map { it.first() }
    }

    override val articles: Flowable<RealmResults<FAQArticle>>
        get() =  realm.where(FAQArticle::class.java)
                .findAll()
                .asFlowable()
                .filter{ it.isLoaded }
}
