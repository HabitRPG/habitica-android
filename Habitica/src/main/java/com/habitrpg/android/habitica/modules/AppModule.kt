package com.habitrpg.android.habitica.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.SoundFileLoader
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.helpers.KeyHelper.Companion.getInstance
import com.habitrpg.shared.habitica.HLogger
import dagger.Module
import dagger.Provides
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {
    @Provides
    @Singleton
    fun providesContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

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
        context: Context,
        sharedPreferences: SharedPreferences,
        keyStore: KeyStore?
    ): KeyHelper? {
        return if (keyStore == null) {
            null
        } else getInstance(context, sharedPreferences, keyStore)
    }

    @Provides
    fun providesResources(context: Context): Resources {
        return context.resources
    }

    @Provides
    fun providesSoundFileLoader(context: Context): SoundFileLoader {
        return SoundFileLoader(context)
    }

    @Provides
    @Singleton
    fun providesSoundManager(): SoundManager {
        return SoundManager()
    }

    @Provides
    @Singleton
    fun pushNotificationManager(
        apiClient: ApiClient,
        sharedPreferences: SharedPreferences,
        context: Context
    ): PushNotificationManager {
        return PushNotificationManager(apiClient, sharedPreferences, context)
    }

    @Provides
    @Singleton
    fun providesRemoteConfigManager(contentRepository: ContentRepository?): AppConfigManager {
        return AppConfigManager(contentRepository)
    }

    companion object {
        const val NAMED_USER_ID = "userId"
    }
}
