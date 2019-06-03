package com.habitrpg.android.habitica;

import com.habitrpg.android.habitica.modules.UserRepositoryModule;

import org.mockito.Mockito;

import dagger.Provides;
import io.realm.Realm;

class TestRepositoryModule extends UserRepositoryModule {


    @Provides
    @Override
    public Realm providesRealm() {
        return Mockito.mock(Realm.class);
    }
}
