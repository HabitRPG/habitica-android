package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.BaseRepository;
import com.habitrpg.android.habitica.data.local.BaseLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;

public abstract class BaseRepositoryImpl<T extends BaseLocalRepository> implements BaseRepository {

    protected final T localRepository;
    protected final ApiService apiService;

    public BaseRepositoryImpl(T localRepository, ApiService apiService) {
        this.localRepository = localRepository;
        this.apiService = apiService;
    }

    public void close() {
        this.localRepository.close();
    }
}
