package com.habitrpg.android.habitica;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.facebook.FacebookSdk;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.PurchaseVerifier;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by Negue on 14.06.2015.
 */
public class HabiticaApplication extends Application {

    public static String Purchase20Gems = "com.habitrpg.android.habitica.iap.20.gems";

    public static HabiticaApplication Instance;
    public static HabitRPGUser User;

    public static APIHelper ApiHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        Instance = this;

        // LeakCanary 1.3.1 has problems on Marshmallow; can remove check once updated with fixes
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            LeakCanary.install(this);
        }

        createBillingAndCheckout();

        FlowManager.init(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        registerActivityLifecycleCallbacks();
    }

    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                HabiticaApplication.currentActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                HabiticaApplication.currentActivity = activity;
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
                if(currentActivity == activity)
                    currentActivity = null;
            }
        });
    }

    public static Activity currentActivity = null;

    // region SQLite overrides

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

    @Override
    public boolean deleteDatabase(String name) {
        if(!name.endsWith(".db")){
            name += ".db";
        }

        FlowManager.destroy();
        reflectionHack(getApplicationContext());

        boolean deleted = super.deleteDatabase(getDatabasePath(name).getAbsolutePath());

        if (deleted) {
            Log.i("hack","Database deleted");
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

    public static boolean exists(@NonNull Context context) {
        try {
            File dbFile = context.getDatabasePath(String.format("%s.db", HabitDatabase.NAME));
            return dbFile.exists();
        } catch (Exception exception) {
            Log.e("DbExists", "Database %s doesn't exist.", exception);
            return false;
        }
    }

    private static void setFinalStatic(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        field.set(null, newValue);
    }

    @Override
    public File getDatabasePath(String name) {
        return new File(getExternalFilesDir(null), "HabiticaDatabase/" + name);
    }

    public static void logout(Context context) {
        Instance.deleteDatabase(HabitDatabase.NAME);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean use_reminder = preferences.getBoolean("use_reminder", false);
        String reminder_time = preferences.getString("reminder_time", "19:00");
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putBoolean("use_reminder", use_reminder);
        editor.putString("reminder_time", reminder_time);
        editor.commit();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean checkUserAuthentication(Context context, HostConfig hostConfig) {
        if (hostConfig == null || hostConfig.getApi() == null || hostConfig.getApi().equals("") || hostConfig.getUser() == null || hostConfig.getUser().equals("")) {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

            return false;
        }

        return true;
    }

    // endregion

    // region IAP - Specific

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

            @Override
            public PurchaseVerifier getPurchaseVerifier() {
                return new HabiticaPurchaseVerifier(HabiticaApplication.this);
            }
        });

        checkout = Checkout.forApplication(billing, Products.create().add(ProductTypes.IN_APP, Arrays.asList(Purchase20Gems)));
    }

    /**
     * For better performance billing class should be used as singleton
     */
    @NonNull
    private Billing billing;

    /**
     * Application wide {@link org.solovyev.android.checkout.Checkout} instance (can be used anywhere in the app).
     * This instance contains all available products in the app.
     */
    @NonNull
    private Checkout checkout;


    @NonNull
    public Checkout getCheckout() {
        return checkout;
    }

    // endregion

    public static void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) currentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = currentActivity.getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
