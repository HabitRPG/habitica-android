package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.BaseRepository;
import com.habitrpg.android.habitica.data.local.BaseLocalRepository;

import java.util.List;

import io.realm.RealmObject;

public abstract class BaseRepositoryImpl<T extends BaseLocalRepository> implements BaseRepository {

    protected final T localRepository;
    protected final ApiClient apiClient;

    public BaseRepositoryImpl(T localRepository, ApiClient apiClient) {
        this.localRepository = localRepository;
        this.apiClient = apiClient;
    }

    public void close() {
        this.localRepository.close();
    }

    @Override
    public <T extends RealmObject> List<T> getUnmanagedCopy(List<T> list) {
        return localRepository.getUnmanagedCopy(list);
    }

    @Override
    public <T extends RealmObject> T getUnmanagedCopy(T object) {
        return localRepository.getUnmanagedCopy(object);
    }
}
