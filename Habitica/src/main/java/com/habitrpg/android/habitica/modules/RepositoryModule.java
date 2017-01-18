package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.implementation.TaskRepositoryImpl;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.DbFlowTaskLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;

import dagger.Provides;

public class RepositoryModule {

    @Provides
    TaskLocalRepository providesTaskLocalRepository() {
        return new DbFlowTaskLocalRepository();
    }

    @Provides
    TaskRepository providesTaskRepository(TaskLocalRepository localRepository, ApiService apiService) {
        return new TaskRepositoryImpl(localRepository, apiService);
    }
}
