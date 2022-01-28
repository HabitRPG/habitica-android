package com.habitrpg.android.habitica.modules

import android.content.Context
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmContentLocalRepository
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.implementation.ContentRepositoryImpl
import dagger.Module
import dagger.Provides
import io.realm.Realm

@Module
open class RepositoryModule {
    @Provides
    open fun providesRealm(): Realm? {
        return Realm.getDefaultInstance()
    }

    @Provides
    fun providesContentLocalRepository(realm: Realm?): ContentLocalRepository {
        return RealmContentLocalRepository(realm!!)
    }

    @Provides
    fun providesContentRepository(
        contentLocalRepository: ContentLocalRepository,
        apiClient: ApiClient,
        context: Context
    ): ContentRepository {
        return ContentRepositoryImpl(
            contentLocalRepository,
            apiClient,
            context
        )
    }
}