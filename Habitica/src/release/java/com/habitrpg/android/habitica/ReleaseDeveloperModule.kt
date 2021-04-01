package com.habitrpg.android.habitica

import com.habitrpg.android.habitica.modules.DeveloperModule
import com.habitrpg.android.habitica.proxy.AnalyticsManagerImpl
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import android.content.Context

//change debug proxy here by override methods
class ReleaseDeveloperModule : DeveloperModule() {
    @Override
    protected fun provideAnalyticsManager(context: Context?): AnalyticsManager {
        return AnalyticsManagerImpl()
    }
}