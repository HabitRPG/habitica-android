package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.FAQArticle
import io.reactivex.rxjava3.core.Flowable

interface FAQRepository : BaseRepository {
    fun getArticles(): Flowable<out List<FAQArticle>>
    fun getArticle(position: Int): Flowable<FAQArticle>
}
