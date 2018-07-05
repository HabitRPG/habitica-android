package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.FAQArticle

import io.reactivex.Flowable
import io.realm.RealmResults

interface FAQLocalRepository : ContentLocalRepository {

    val articles: Flowable<RealmResults<FAQArticle>>
}
