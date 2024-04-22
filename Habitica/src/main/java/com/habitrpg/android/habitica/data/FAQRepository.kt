package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.FAQArticle
import kotlinx.coroutines.flow.Flow

interface FAQRepository : BaseRepository {
    fun getArticles(): Flow<List<FAQArticle>>

    fun getArticle(position: Int): Flow<FAQArticle>
}
