package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.FAQRepository;
import com.habitrpg.android.habitica.data.local.FAQLocalRepository;
import com.habitrpg.android.habitica.models.FAQArticle;

import java.util.List;

import rx.Observable;


public class FAQRepositoryImpl extends ContentRepositoryImpl<FAQLocalRepository> implements FAQRepository {
    public FAQRepositoryImpl(FAQLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<List<FAQArticle>> getArticles() {
        return localRepository.getArticles();
    }
}
