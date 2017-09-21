package com.habitrpg.android.habitica;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.amplitude.api.Identify;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.habitrpg.android.habitica.api.HostConfig;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.activities.IntroActivity;
import com.habitrpg.android.habitica.ui.activities.LoginActivity;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.PurchaseVerifier;

import java.lang.reflect.Field;

import javax.inject.Inject;

import dagger.Lazy;
import io.realm.Realm;
import io.realm.RealmConfiguration;

//contains all HabiticaApplicationLogic except dagger componentInitialisation
public abstract class HabiticaBaseApplication extends MultiDexApplication {

    private static AppComponent component;
    public RefWatcher refWatcher;
    @Inject
    ApiClient lazyApiHelper;
    @Inject
    InventoryRepository inventoryRepository;
    @Inject
    SharedPreferences sharedPrefs;
    @Inject
    CrashlyticsProxy crashlyticsProxy;
    /**
     * For better performance billing class should be used as singleton
     */
    private Billing billing;
    /**
     * Application wide {@link Checkout} instance (can be used
     * anywhere in the app).
     * This instance contains all available products in the app.
     */
    private Checkout checkout;

    public static HabiticaBaseApplication getInstance(Context context) {
        return (HabiticaBaseApplication) context.getApplicationContext();
    }

    public static void logout(Context context) {
        Realm realm = Realm.getDefaultInstance();
        getInstance(context).deleteDatabase(realm.getPath());
        realm.close();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean use_reminder = preferences.getBoolean("use_reminder", false);
        String reminder_time = preferences.getString("reminder_time", "19:00");
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putBoolean("use_reminder", use_reminder);
        editor.putString("reminder_time", reminder_time);
        editor.apply();
        getInstance(context).lazyApiHelper.updateAuthenticationCredentials(null, null);
        startActivity(LoginActivity.class, context);
    }

    public static boolean checkUserAuthentication(Context context, HostConfig hostConfig) {
        if (hostConfig == null || hostConfig.getApi() == null || hostConfig.getApi().equals("") || hostConfig.getUser() == null || hostConfig.getUser().equals("")) {
            startActivity(IntroActivity.class, context);

            return false;
        }

        return true;
    }

    private static void startActivity(Class activityClass, Context context) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static AppComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        setupRealm();
        setupDagger();
        setupLeakCanary();
        setupFacebookSdk();
        createBillingAndCheckout();
        HabiticaIconsHelper.init(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (!BuildConfig.DEBUG) {
            try {
                Amplitude.getInstance().initialize(this, getString(R.string.amplitude_app_id)).enableForegroundTracking(this);
                Identify identify = new Identify().setOnce("androidStore", BuildConfig.STORE);
                Amplitude.getInstance().identify(identify);
            } catch (Resources.NotFoundException ignored) {
            }
        }
        Fresco.initialize(this);

        RxErrorHandler.init(crashlyticsProxy);

        checkIfNewVersion();
    }

    protected void setupRealm() {
        Realm.init(this);
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded();
        try {
            Realm.setDefaultConfiguration(builder.build());
        } catch (UnsatisfiedLinkError ignored) {
            //Catch crash in tests
        }
    }

    private void checkIfNewVersion() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MyApplication", "couldn't get package info!");
        }

        if (info == null) {
            return;
        }

        int lastInstalledVersion = sharedPrefs.getInt("last_installed_version", 0);
        if (lastInstalledVersion < info.versionCode) {
            sharedPrefs.edit().putInt("last_installed_version", info.versionCode).apply();
            inventoryRepository.retrieveContent().subscribe(contentResult -> {}, RxErrorHandler.handleEmptyError());
        }
    }

    private void setupDagger() {
        component = initDagger();
        component.inject(this);
    }

    protected abstract AppComponent initDagger();

    private void setupLeakCanary() {
        refWatcher = LeakCanary.install(this);
    }

    private void setupFacebookSdk() {
        String fbApiKey = null;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            fbApiKey = bundle.getString(FacebookSdk.APPLICATION_ID_PROPERTY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("FB Error", "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("FB Error", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        if (fbApiKey != null) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name,
                                               int mode, SQLiteDatabase.CursorFactory factory) {
        return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name,
                                               int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory, errorHandler);
    }

    // endregion

    // region IAP - Specific

    @Override
    public boolean deleteDatabase(String name) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.deleteAll();
            realm1.close();
        });
        return true;
    }

    private void createBillingAndCheckout() {
        billing = new Billing(this, new Billing.DefaultConfiguration() {
            @NonNull
            @Override
            public String getPublicKey() {
                return "DONT-NEED-IT";
            }

            @Nullable
            @Override
            public Cache getCache() {
                return Billing.newCache();
            }

            @NonNull
            @Override
            public PurchaseVerifier getPurchaseVerifier() {
                return new HabiticaPurchaseVerifier(HabiticaBaseApplication.this, lazyApiHelper);
            }
        });


        checkout = Checkout.forApplication(billing);
    }

    @NonNull
    public Checkout getCheckout() {
        return checkout;
    }

    // endregion

    public Billing getBilling() {
        return billing;
    }
}
