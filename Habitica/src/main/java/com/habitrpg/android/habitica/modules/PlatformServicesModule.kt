package com.habitrpg.android.habitica.modules

import android.content.Context
import com.habitrpg.android.habitica.helpers.CrashReporter
import com.habitrpg.android.habitica.helpers.FirebaseCrashReporter
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.Navigator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class PlatformServicesModule {
    @Binds
    abstract fun bindCrashReporter(reporter: FirebaseCrashReporter): CrashReporter

    companion object {
        @Provides
        fun provideNavigator(): Navigator = MainNavigationController
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PlatformServicesEntryPoint {
    fun navigator(): Navigator
}

val Context.platformServices: PlatformServicesEntryPoint
    get() = EntryPointAccessors.fromApplication(applicationContext, PlatformServicesEntryPoint::class.java)

val Context.navigator: Navigator
    get() = platformServices.navigator()
