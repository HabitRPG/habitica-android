package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.FAQArticle

import io.reactivex.rxjava3.core.Flowable
import io.realm.RealmResults

interface FAQRepository : BaseRepository {
    fun getArticles(): Flowable<RealmResults<FAQArticle>>
    fun getArticle(position: Int): Flowable<FAQArticle>
}
