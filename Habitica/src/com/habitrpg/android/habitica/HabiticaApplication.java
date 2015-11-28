package com.habitrpg.android.habitica;

import android.app.Application;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.instabug.library.Instabug;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.PurchaseVerifier;
import org.solovyev.android.checkout.RequestListener;
import com.facebook.FacebookSdk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Negue on 14.06.2015.
 */
public class HabiticaApplication extends Application {

    public static HabiticaApplication Instance;

    @Override
    public void onCreate() {
        super.onCreate();

        Instance = this;

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
        return super.deleteDatabase(getDatabasePath(name).getAbsolutePath());
    }

    @Override
    public File getDatabasePath(String name) {
        return new File(getExternalFilesDir(null), "HabiticaDatabase/" + name);
    }

    // endregion


    /**
     * For better performance billing class should be used as singleton
     */
    @NonNull
    private final Billing billing = new Billing(this, new Billing.Configuration() {
        @NonNull
        @Override
        public String getPublicKey() {
            return "sdfsdfdfsd";
        }

        @Nullable
        @Override
        public Cache getCache() {
            return Billing.newCache();
        }

        @Override
        public PurchaseVerifier getPurchaseVerifier() {
            return new PurchaseVerifier() {
                @Override
                public void verify(List<Purchase> purchases, RequestListener<List<Purchase>> requestListener) {
                    final List<Purchase> verifiedPurchases = new ArrayList<Purchase>(purchases.size());
                   /* for (Purchase purchase : purchases) {
                        if (Security.verifyPurchase(publicKey, purchase.data, purchase.signature)) {
                            verifiedPurchases.add(purchase);
                        } else {
                            if (isEmpty(purchase.signature)) {
                                Billing.error("Cannot verify purchase: " + purchase + ". Signature is empty");
                            } else {
                                Billing.error("Cannot verify purchase: " + purchase + ". Wrong signature");
                            }
                        }
                    }*/
                    //requestListener.onSuccess(verifiedPurchases);
                }
            };
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

    public static String Purchase20Gems = "com.habitrpg.android.habitica.buy20gems";

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

}
