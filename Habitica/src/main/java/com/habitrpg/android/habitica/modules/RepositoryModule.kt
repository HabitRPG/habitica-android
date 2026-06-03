package com.habitrpg.android.habitica.modules

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.implementation.ContentRepositoryImpl
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmContentLocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
open class RepositoryModule {

    @Singleton
    @Provides
    fun initRealm(@ApplicationContext context: Context): RealmConfiguration {
        Realm.init(context)
        val builder =
            RealmConfiguration.Builder()
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .compactOnLaunch { totalBytes, usedBytes ->
                    // Compact if the file is over 100MB in size and less than 50% 'used'
                    val oneHundredMB = 50 * 1024 * 1024
                    (totalBytes > oneHundredMB) && (usedBytes / totalBytes) < 0.5
                }
        return builder.build()
    }
    @Provides
    open fun providesRealm(config: RealmConfiguration): Realm {
        return Realm.getInstance(config)
    }

    @Provides
    fun providesContentLocalRepository(realm: Realm): ContentLocalRepository {
        return RealmContentLocalRepository(realm)
    }

    @Provides
    fun providesContentRepository(
        contentLocalRepository: ContentLocalRepository,
        apiClient: ApiClient,
        @ApplicationContext context: Context,
        authenticationHandler: AuthenticationHandler
    ): ContentRepository {
        return ContentRepositoryImpl(
            contentLocalRepository,
            apiClient,
            context,
            authenticationHandler
        )
    }
}
