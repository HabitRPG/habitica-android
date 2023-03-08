package com.habitrpg.android.habitica.components

import com.habitrpg.android.habitica.modules.UserModule
import com.habitrpg.android.habitica.modules.UserRepositoryModule
import dagger.Subcomponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ServiceScoped

@ActivityRetainedScoped
@ServiceScoped
@Subcomponent(modules = [UserModule::class, UserRepositoryModule::class])
interface UserComponent {
}
