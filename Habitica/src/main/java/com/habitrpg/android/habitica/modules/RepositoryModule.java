package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.ContentRepository;
import com.habitrpg.android.habitica.data.implementation.ContentRepositoryImpl;
import com.habitrpg.android.habitica.data.local.ContentLocalRepository;
import com.habitrpg.android.habitica.data.local.implementation.RealmContentLocalRepository;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module
public class RepositoryModule {
    @Provides
    public Realm providesRealm() {
        return Realm.getDefaultInstance();
    }

    @Provides
    public ContentLocalRepository providesContentLocalRepository(Realm realm) { return new RealmContentLocalRepository(realm); }

    @Provides
    public ContentRepository providesContentRepository(ContentLocalRepository contentLocalRepository, ApiClient apiClient) { return new ContentRepositoryImpl<ContentLocalRepository>(contentLocalRepository, apiClient) {}; }
}
