package com.habitrpg.android.habitica.components

import com.habitrpg.android.habitica.modules.ApiModule
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.modules.DeveloperModule
import com.habitrpg.android.habitica.modules.RepositoryModule
import com.habitrpg.android.habitica.modules.UserModule
import com.habitrpg.android.habitica.modules.UserRepositoryModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DeveloperModule::class, AppModule::class, ApiModule::class, RepositoryModule::class])
interface AppComponent {
    fun plus(
        userModule : UserModule?,
        userRepositoryModule : UserRepositoryModule?
    ) : UserComponent?
}
