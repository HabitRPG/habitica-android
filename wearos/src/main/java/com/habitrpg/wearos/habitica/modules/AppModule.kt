package com.habitrpg.wearos.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.helpers.AppTestingLevel
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.data.HabiticaConverterFactory
import com.habitrpg.wearos.habitica.managers.AppStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Converter
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providesHostConfig(
        sharedPreferences: SharedPreferences,
        keyHelper: KeyHelper?,
        @ApplicationContext context: Context,
    ): HostConfig = HostConfig(sharedPreferences, keyHelper, context)

    @Provides
    fun providesConverterFactory(): Converter.Factory = HabiticaConverterFactory()

    @Provides
    @Singleton
    fun providesApiHClient(
        hostConfig: HostConfig,
        @ApplicationContext context: Context,
        converter: Converter.Factory,
        appStateManager: AppStateManager,
    ): ApiClient =
        ApiClient(
            converter,
            hostConfig,
            appStateManager,
            context,
        )

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
            KeyHelper.getInstance(context, sharedPreferences, keyStore)
        }

    @Provides
    @Singleton
    fun provideTestingLevel(): AppTestingLevel = AppTestingLevel.valueOf(BuildConfig.TESTING_LEVEL.uppercase())
}
