package com.habitrpg.android.habitica.modules;


import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.SetupCustomizationRepository;
import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.implementation.ChallengeRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.SetupCustomizationRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.TagRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.TaskRepositoryImpl;
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.DbFlowChallengeLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.DbFlowTaskLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.TagLocalRepositoryImpl;
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
    @Singleton
    ChallengeLocalRepository provideChallengeLocalRepository(){
        return new DbFlowChallengeLocalRepository();
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

    @Provides
    @Singleton
    ChallengeRepository providesChallengeRepository(ChallengeLocalRepository localRepository, ApiClient apiClient) {
        return new ChallengeRepositoryImpl(localRepository, apiClient);
    }


    @Provides
    TagLocalRepository providesTagLocalRepository() {
        return new TagLocalRepositoryImpl();
    }

    @Provides
    TagRepository providesTagRepository(TagLocalRepository localRepository, ApiClient apiClient) {
        return new TagRepositoryImpl(localRepository, apiClient);
    }
}
