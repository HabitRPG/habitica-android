package com.habitrpg.android.habitica;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.FacebookSdk;
import com.instabug.library.Instabug;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.PurchaseVerifier;
import org.solovyev.android.checkout.RequestListener;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Created by Negue on 14.06.2015.
 */
public class HabiticaApplication extends Application {

    public static HabiticaApplication Instance;
    public static HabitRPGUser User;

    public static APIHelper ApiHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        Instance = this;

        billing.connect();

        FlowManager.init(this);

        FacebookSdk.sdkInitialize(getApplicationContext());

        Instabug.DEBUG = BuildConfig.DEBUG;
        Instabug.initialize(this, "a5aa5f471a9cd8a958c0c55181172655");
    }

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
        return super.deleteDatabase(getDatabasePath(name).getAbsolutePath());
    }

    @Override
    public File getDatabasePath(String name) {
        return new File(getExternalFilesDir(null), "HabiticaDatabase/" + name);
    }

    public static void logout(Context context){
        Instance.deleteDatabase(HabitDatabase.NAME);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    public static void checkUserAuthentication(Context context, HostConfig hostConfig){
        if (hostConfig == null || hostConfig.getApi() == null || hostConfig.getApi().equals("") || hostConfig.getUser() == null || hostConfig.getUser().equals("")) {
            context.startActivity(new Intent(context, LoginActivity.class));
        }
    }

    // endregion

    // region IAP - Specific

    /**
     * For better performance billing class should be used as singleton
     */
    @NonNull
    private final Billing billing = new Billing(this, new Billing.Configuration() {
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
            return new HabiticaPurchaseVerifier();
        }

        @Override
        public Inventory getFallbackInventory(Checkout checkout, Executor executor) {
            return null;
        }

        @Override
        public boolean isAutoConnect() {
            return false;
        }
    });

    public static String Purchase20Gems = "com.habitrpg.android.habitica.iap.20.gems";

    /**
     * Application wide {@link org.solovyev.android.checkout.Checkout} instance (can be used anywhere in the app).
     * This instance contains all available products in the app.
     */
    @NonNull
    private final Checkout checkout = Checkout.forApplication(billing, Products.create().add(ProductTypes.IN_APP, Arrays.asList(Purchase20Gems)));


    @NonNull
    public Checkout getCheckout() {
        return checkout;
    }

    // endregion
}
