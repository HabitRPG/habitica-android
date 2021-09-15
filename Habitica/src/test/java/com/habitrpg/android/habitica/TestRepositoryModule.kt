package com.habitrpg.android.habitica

import com.habitrpg.android.habitica.modules.RepositoryModule
import dagger.Provides
import io.mockk.mockk
import io.realm.Realm

internal class TestRepositoryModule : RepositoryModule() {
    @Provides
    override fun providesRealm(): Realm {
        return mockk()
    }
}
