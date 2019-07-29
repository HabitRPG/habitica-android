package com.habitrpg.android.habitica.components;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.modules.ApiModule;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.modules.DeveloperModule;
import com.habitrpg.android.habitica.modules.RepositoryModule;
import com.habitrpg.android.habitica.modules.UserRepositoryModule;
import com.habitrpg.android.habitica.modules.UserModule;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DeveloperModule.class, AppModule.class, ApiModule.class, RepositoryModule.class})
public interface AppComponent {

    UserComponent plus(UserModule userModule, UserRepositoryModule userRepositoryModule);

    void inject(@NotNull HabiticaBaseApplication habiticaBaseApplication);
}
