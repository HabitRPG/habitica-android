package com.habitrpg.android.habitica.modules

import android.content.Context
import com.habitrpg.android.habitica.proxy.implementation.EmptyAnalyticsManager
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// provide proxy class for libraries(to avoid 65k limit)
@InstallIn(SingletonComponent::class)
@Module
open class DeveloperModule {
    @Provides
    @Singleton
    open fun provideAnalyticsManager(@ApplicationContext context: Context): AnalyticsManager {
        return EmptyAnalyticsManager()
    }
}
