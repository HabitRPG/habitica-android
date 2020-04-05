package com.habitrpg.android.habitica;

import com.habitrpg.android.habitica.modules.RepositoryModule;

import org.mockito.Mockito;

import dagger.Provides;
import io.realm.Realm;

class TestRepositoryModule extends RepositoryModule {

    @Provides
    @Override
    public Realm providesRealm() {
        return Mockito.mock(Realm.class);
    }
}
