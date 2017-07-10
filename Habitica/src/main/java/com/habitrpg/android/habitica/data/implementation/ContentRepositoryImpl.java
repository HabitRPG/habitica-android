package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.ContentRepository;
import com.habitrpg.android.habitica.data.local.BaseLocalRepository;
import com.habitrpg.android.habitica.data.local.ContentLocalRepository;
import com.habitrpg.android.habitica.models.ContentResult;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.Observable;

abstract class ContentRepositoryImpl<T extends ContentLocalRepository> extends BaseRepositoryImpl<T> implements ContentRepository {

    private Date lastSync = null;

    public ContentRepositoryImpl(T localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<ContentResult> retrieveContent() {
        return retrieveContent(false);
    }

    @Override
    public Observable<ContentResult> retrieveContent(boolean forced) {
        if (forced || this.lastSync == null || (new Date().getTime() - this.lastSync.getTime()) > 3600000) {
            lastSync = new Date();
            return apiClient.getContent()
                    .doOnNext(localRepository::saveContent);
        } else {
            return Observable.just(null);
        }

    }

}
