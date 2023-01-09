package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import kotlinx.coroutines.flow.Flow

class FAQRepositoryImpl(localRepository: FAQLocalRepository, apiClient: ApiClient, userID: String) : BaseRepositoryImpl<FAQLocalRepository>(localRepository, apiClient, userID), FAQRepository {
    override fun getArticle(position: Int): Flow<FAQArticle> {
        return localRepository.getArticle(position)
    }

    override fun getArticles(): Flow<List<FAQArticle>> {
        return localRepository.articles
    }
}
