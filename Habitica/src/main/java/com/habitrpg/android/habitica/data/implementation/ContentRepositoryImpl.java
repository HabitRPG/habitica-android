package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.ContentRepository;
import com.habitrpg.android.habitica.data.local.BaseLocalRepository;
import com.habitrpg.android.habitica.data.local.ContentLocalRepository;
import com.habitrpg.android.habitica.models.ContentResult;

import rx.Observable;

abstract class ContentRepositoryImpl<T extends ContentLocalRepository> extends BaseRepositoryImpl<T> implements ContentRepository {

    public ContentRepositoryImpl(T localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<ContentResult> retrieveContent() {
        return apiClient.getContent()
                .doOnNext(localRepository::saveContent);
    }

}
