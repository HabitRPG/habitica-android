package com.habitrpg.android.habitica.modules;


import android.content.Context;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.CustomizationRepository;
import com.habitrpg.android.habitica.data.FAQRepository;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.SetupCustomizationRepository;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.TutorialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.data.implementation.ChallengeRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.CustomizationRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.FAQRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.InventoryRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.SetupCustomizationRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.SocialRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.TagRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.TaskRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.TutorialRepositoryImpl;
import com.habitrpg.android.habitica.data.implementation.UserRepositoryImpl;
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository;
import com.habitrpg.android.habitica.data.local.FAQLocalRepository;
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository;
import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository;
import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmChallengeLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmCustomizationLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmFAQLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmInventoryLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmSocialLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmTagLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmTaskLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmTutorialLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmUserLocalRepository;
import com.habitrpg.android.habitica.helpers.AppConfigManager;
import com.habitrpg.android.habitica.helpers.UserScope;
import com.habitrpg.android.habitica.proxy.AnalyticsManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module
public class UserRepositoryModule {

    @Provides
    SetupCustomizationRepository providesSetupCustomizationRepository(Context context) {
        return new SetupCustomizationRepositoryImpl(context);
    }

    @Provides
    TaskLocalRepository providesTaskLocalRepository(Realm realm) {
        return new RealmTaskLocalRepository(realm);
    }

    @Provides
    TaskRepository providesTaskRepository(TaskLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId, AppConfigManager appConfigManager, AnalyticsManager analyticsManager) {
        return new TaskRepositoryImpl(localRepository, apiClient, userId, appConfigManager, analyticsManager);
    }

    @Provides
    TagLocalRepository providesTagLocalRepository(Realm realm) {
        return new RealmTagLocalRepository(realm);
    }

    @Provides
    TagRepository providesTagRepository(TagLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId) {
        return new TagRepositoryImpl(localRepository, apiClient, userId);
    }

    @Provides
    ChallengeLocalRepository provideChallengeLocalRepository(Realm realm){
        return new RealmChallengeLocalRepository(realm);
    }

    @Provides
    ChallengeRepository providesChallengeRepository(ChallengeLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId) {
        return new ChallengeRepositoryImpl(localRepository, apiClient, userId);
    }

    @Provides
    UserLocalRepository providesUserLocalRepository(Realm realm) {
        return new RealmUserLocalRepository(realm);
    }

    @Provides
    UserRepository providesUserRepository(UserLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId, TaskRepository taskRepository, AppConfigManager appConfigManager) {
        return new UserRepositoryImpl(localRepository, apiClient, userId, taskRepository, appConfigManager);
    }

    @Provides
    SocialLocalRepository providesSocialLocalRepository(Realm realm) {
        return new RealmSocialLocalRepository(realm);
    }

    @Provides
    SocialRepository providesSocialRepository(SocialLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId) {
        return new SocialRepositoryImpl(localRepository, apiClient, userId);
    }

    @Provides
    InventoryLocalRepository providesInventoryLocalRepository(Realm realm, Context context) {
        return new RealmInventoryLocalRepository(realm);
    }

    @Provides
    InventoryRepository providesInventoryRepository(InventoryLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId, AppConfigManager remoteConfig) {
        return new InventoryRepositoryImpl(localRepository, apiClient, userId, remoteConfig);
    }

    @Provides
    FAQLocalRepository providesFAQLocalRepository(Realm realm) {
        return new RealmFAQLocalRepository(realm);
    }

    @Provides
    FAQRepository providesFAQRepository(FAQLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId) {
        return new FAQRepositoryImpl(localRepository, apiClient, userId);
    }

    @Provides
    TutorialLocalRepository providesTutorialLocalRepository(Realm realm) {
        return new RealmTutorialLocalRepository(realm);
    }

    @Provides
    TutorialRepository providesTutorialRepository(TutorialLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId) {
        return new TutorialRepositoryImpl(localRepository, apiClient, userId);
    }

    @Provides
    CustomizationLocalRepository providesCustomizationLocalRepository(Realm realm) {
        return new RealmCustomizationLocalRepository(realm);
    }

    @Provides
    CustomizationRepository providesCustomizationRepository(CustomizationLocalRepository localRepository, ApiClient apiClient, @Named(AppModule.NAMED_USER_ID) String userId) {
        return new CustomizationRepositoryImpl(localRepository, apiClient, userId);
    }
}
