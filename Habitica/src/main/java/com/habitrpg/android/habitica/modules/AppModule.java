package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaPurchaseVerifier;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.TagsHelper;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.PurchaseVerifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;

import java.util.Arrays;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private HabiticaApplication application;

    public AppModule(HabiticaApplication application) {
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
    @Named("UserID")
    public String providesUserID(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(application.getString(R.string.SP_userID), null);
    }

    @Provides
    @Singleton
    public TagsHelper providesTagsHelper() {
        return new TagsHelper();
    }

    @Provides
    @Singleton
    public Billing providesBilling() {
        return new Billing(application, new Billing.DefaultConfiguration() {
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
                return new HabiticaPurchaseVerifier(application);
            }
        });
    }

    @Provides
    @Singleton
    public Checkout providesCheckout(Billing billing) {
        return Checkout.forApplication(billing, Products.create().add(ProductTypes.IN_APP, Arrays.asList(HabiticaApplication.Purchase20Gems)));

    }
}
