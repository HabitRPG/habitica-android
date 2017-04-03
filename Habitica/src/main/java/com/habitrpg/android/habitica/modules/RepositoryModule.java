package com.habitrpg.android.habitica.modules;


import com.habitrpg.android.habitica.data.SetupCustomizationRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.implementation.SetupCustomizationRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.TaskRepositoryImpl;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.DbFlowTaskLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    @Provides
    SetupCustomizationRepository providesSetupCustomizationRepository(Context context) {
        return new SetupCustomizationRepositoryImpl(context);

    }

    @Provides
    TaskLocalRepository providesTaskLocalRepository() {
        return new DbFlowTaskLocalRepository();
    }

    @Provides
    @Singleton
    TaskRepository providesTaskRepository(TaskLocalRepository localRepository, ApiClient apiClient) {
        return new TaskRepositoryImpl(localRepository, apiClient);
    }
}
