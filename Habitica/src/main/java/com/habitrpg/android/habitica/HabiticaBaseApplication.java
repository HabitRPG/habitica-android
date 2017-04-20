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
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.activities.IntroActivity;
import com.habitrpg.android.habitica.ui.activities.LoginActivity;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.PurchaseVerifier;

import java.io.File;
import java.lang.reflect.Field;

import javax.inject.Inject;

import dagger.Lazy;
import io.realm.Realm;
import io.realm.RealmConfiguration;

//contains all HabiticaApplicationLogic except dagger componentInitialisation
public abstract class HabiticaBaseApplication extends MultiDexApplication {

    public static com.habitrpg.android.habitica.models.user.User User;
    public static Activity currentActivity = null;
    private static AppComponent component;
    public RefWatcher refWatcher;
    @Inject
    Lazy<ApiClient> lazyApiHelper;
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

    public static boolean exists(@NonNull Context context) {
        try {
            File dbFile = context.getDatabasePath(String.format("%s.db", HabitDatabase.NAME));
            return dbFile.exists();
        } catch (Exception exception) {
            Log.e("DbExists", "Database %s doesn't exist.", exception);
            return false;
        }
    }

    private static void setFinalStatic(Field field, @Nullable Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        field.set(null, newValue);
    }

    public static void logout(Context context) {
        getInstance(context).deleteDatabase(HabitDatabase.NAME);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean use_reminder = preferences.getBoolean("use_reminder", false);
        String reminder_time = preferences.getString("reminder_time", "19:00");
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putBoolean("use_reminder", use_reminder);
        editor.putString("reminder_time", reminder_time);
        editor.apply();
        getInstance(context).lazyApiHelper.get().updateAuthenticationCredentials(null, null);
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

    // region SQLite overrides

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
        crashlyticsProxy.init(this);
        setupLeakCanary();
        setupFlowManager();
        setupFacebookSdk();
        createBillingAndCheckout();
        registerActivityLifecycleCallbacks();

        if (!BuildConfig.DEBUG) {
            try {
                Amplitude.getInstance().initialize(this, getString(R.string.amplitude_app_id)).enableForegroundTracking(this);
            } catch (Resources.NotFoundException e) {
                //pass
            }
        }

        Fresco.initialize(this);
        checkIfNewVersion();
    }

    protected void setupRealm() {
        Realm.init(this);
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder()
                .schemaVersion(1);
        if (BuildConfig.DEBUG) {
            builder = builder.deleteRealmIfMigrationNeeded();
        }
        Realm.setDefaultConfiguration(builder.build());
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
            ApiClient apiClient = this.lazyApiHelper.get();

            apiClient.getContent()
                    .subscribe(contentResult -> { }, throwable -> {});
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

    private void setupFlowManager() {
        FlowManager.init(this);
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

    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                HabiticaBaseApplication.currentActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                HabiticaBaseApplication.currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (currentActivity.equals(activity)) {
                    currentActivity = null;
                }
            }
        });
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
        if (!name.endsWith(".db")) {
            name += ".db";
        }

        FlowManager.destroy();
        reflectionHack(getApplicationContext());

        boolean deleted = super.deleteDatabase(getDatabasePath(name).getAbsolutePath());

        if (deleted) {
            Log.i("hack", "Database deleted");
        } else {
            Log.e("hack", "Database not deleted");
        }

        if (exists(getApplicationContext())) {
            Log.i("hack", "Database exists before FlowManager.init");
        } else {
            Log.i("hack", "Database does not exist before FlowManager.init");
        }

        return deleted;
    }

    // Hack for DBFlow - Not deleting Database
    // https://github.com/kaeawc/dbflow-sample-app/blob/master/app/src/main/java/io/kaeawc/flow/app/ui/MainActivityFragment.java#L201
    private void reflectionHack(@NonNull Context context) {

        try {
            Field field = FlowManager.class.getDeclaredField("mDatabaseHolder");
            setFinalStatic(field, null);
        } catch (NoSuchFieldException noSuchField) {
            Log.e("nosuchfield", "No such field exists in FlowManager", noSuchField);
        } catch (IllegalAccessException illegalAccess) {
            Log.e("illegalaccess", "Illegal access of FlowManager", illegalAccess);
        }

        FlowManager.init(context);

        if (exists(context)) {
            Log.i("Database", "Database exists after FlowManager.init with reflection hack");
        } else {
            Log.i("Database", "Database does not exist after FlowManager.init with reflection hack");
        }
    }

    @Override
    public File getDatabasePath(String name) {
        return new File(getExternalFilesDir(null), "HabiticaDatabase/" + name);
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
                return new HabiticaPurchaseVerifier(HabiticaBaseApplication.this, lazyApiHelper.get());
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
