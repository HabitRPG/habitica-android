package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm

class RealmFAQLocalRepository(realm: Realm) : RealmContentLocalRepository(realm), FAQLocalRepository {
    override fun getArticle(position: Int): Flowable<FAQArticle> {
        return RxJavaBridge.toV3Flowable(
            realm.where(FAQArticle::class.java)
                .equalTo("position", position)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded && it.count() > 0 }
                .map { it.first() }
        )
    }

    override val articles: Flowable<out List<FAQArticle>>
        get() = RxJavaBridge.toV3Flowable(
            realm.where(FAQArticle::class.java)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
        )
}
