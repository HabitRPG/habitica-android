package com.habitrpg.android.habitica.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.preference.PreferenceManager;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.ContentRepository;
import com.habitrpg.android.habitica.executors.JobExecutor;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.executors.UIThread;
import com.habitrpg.android.habitica.helpers.AppConfigManager;
import com.habitrpg.android.habitica.helpers.KeyHelper;
import com.habitrpg.android.habitica.helpers.SoundFileLoader;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    public static final String NAMED_USER_ID = "userId";

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Context providesContext() {
        return application;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Nullable
    KeyStore provideKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Nullable
    KeyHelper provideKeyHelper(Context context, SharedPreferences sharedPreferences, @Nullable KeyStore keyStore) {
        if (keyStore == null) {
            return null;
        }
        return KeyHelper.Companion.getInstance(context, sharedPreferences, keyStore);
    }

    @Provides
    @Singleton
    public TaskFilterHelper providesTagsHelper() {
        return new TaskFilterHelper();
    }

    @Provides
    public Resources providesResources(Context context) {
        return context.getResources();
    }

    @Provides
    public SoundFileLoader providesSoundFileLoader(Context context) {
        return new SoundFileLoader(context);
    }

    @Provides
    @Singleton
    public SoundManager providesSoundManager() {
        return new SoundManager();
    }


    @Provides
    @Singleton
    ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
        return jobExecutor;
    }


    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }


    @Provides
    @Singleton
    PushNotificationManager pushNotificationManager(ApiClient apiClient, SharedPreferences sharedPreferences, Context context) {
        return new PushNotificationManager(apiClient, sharedPreferences, context);
    }

    @Provides
    @Singleton
    AppConfigManager providesRemoteConfigManager(ContentRepository contentRepository) {
        return new AppConfigManager(contentRepository);
    }
}
