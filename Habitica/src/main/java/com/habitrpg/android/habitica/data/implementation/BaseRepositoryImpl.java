package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.BaseRepository;
import com.habitrpg.android.habitica.data.local.BaseLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.IApiClient;

public abstract class BaseRepositoryImpl<T extends BaseLocalRepository> implements BaseRepository {

    protected final T localRepository;
    protected final IApiClient apiClient;

    public BaseRepositoryImpl(T localRepository, IApiClient apiClient) {
        this.localRepository = localRepository;
        this.apiClient = apiClient;
    }

    public void close() {
        this.localRepository.close();
    }
}
