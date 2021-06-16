package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.FAQArticle

import io.reactivex.rxjava3.core.Flowable

interface FAQLocalRepository : ContentLocalRepository {
    fun getArticle(position: Int): Flowable<FAQArticle>

    val articles: Flowable<out List<FAQArticle>>
}
