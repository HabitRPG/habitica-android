package com.habitrpg.android.habitica.modules

import android.content.Context
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.proxy.implementation.EmptyAnalyticsManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

//provide proxy class for libraries(to avoid 65k limit)
@Module
open class DeveloperModule {
    @Provides
    @Singleton
    open fun provideAnalyticsManager(context: Context): AnalyticsManager {
        return EmptyAnalyticsManager()
    }
}