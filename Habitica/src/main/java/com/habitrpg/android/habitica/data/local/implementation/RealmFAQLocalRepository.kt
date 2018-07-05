package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmResults

class RealmFAQLocalRepository(realm: Realm) : RealmContentLocalRepository(realm), FAQLocalRepository {

    override val articles: Flowable<RealmResults<FAQArticle>>
        get() =  realm.where(FAQArticle::class.java)
                .findAll()
                .asFlowable()
                .filter{ it.isLoaded }
}
