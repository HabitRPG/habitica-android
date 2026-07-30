package com.habitrpg.android.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.android.habitica.helpers.SoundFileLoader
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.helpers.KeyHelper.Companion.getInstance
import com.habitrpg.shared.habitica.HLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.inject.Provider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context,
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    fun provideKeyStore(): KeyStore? {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            return keyStore
        } catch (e: KeyStoreException) {
            HLogger.logException("KeyHelper", "Error initializing", e)
        } catch (e: CertificateException) {
            HLogger.logException("KeyHelper", "Error initializing", e)
        } catch (e: NoSuchAlgorithmException) {
            HLogger.logException("KeyHelper", "Error initializing", e)
        } catch (e: IOException) {
            HLogger.logException("KeyHelper", "Error initializing", e)
        }
        return null
    }

    @Provides
    fun provideKeyHelper(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        keyStore: KeyStore?,
    ): KeyHelper? =
        if (keyStore == null) {
            null
        } else {
            getInstance(context, sharedPreferences, keyStore)
        }

    @Provides
    @Singleton
    fun providesAuthenticationHandler(sharedPreferences: SharedPreferences): AuthenticationHandler =
        if (BuildConfig.DEBUG && BuildConfig.TEST_USER_ID.isNotEmpty()) {
            AuthenticationHandler(BuildConfig.TEST_USER_ID)
        } else {
            AuthenticationHandler(sharedPreferences)
        }

    @Provides
    fun providesResources(
        @ApplicationContext context: Context,
    ): Resources = context.resources

    @Provides
    fun providesSoundFileLoader(
        @ApplicationContext context: Context,
    ): SoundFileLoader = SoundFileLoader(context)

    @Provides
    @Singleton
    fun pushNotificationManager(
        apiClient: ApiClient,
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context,
    ): PushNotificationManager = PushNotificationManager(apiClient, sharedPreferences, context)

    @Provides
    @Singleton
    fun providesRemoteConfigManager(
        contentRepository: Provider<ContentRepository>,
        sharedPreferences: SharedPreferences,
    ): AppConfigManager = AppConfigManager(contentRepository, sharedPreferences)

    @Provides
    fun providesReviewManager(
        @ApplicationContext context: Context,
        configManager: AppConfigManager,
    ): ReviewManager = ReviewManager(context, configManager)
}
