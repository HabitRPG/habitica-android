package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Transaction;

import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.adapter.rxjava.HttpException;

/**
 * Created by Negue on 26.11.2015.
 */
public class HabiticaPurchaseVerifier extends BasePurchaseVerifier {

    private static final String PURCHASED_PRODUCTS_KEY = "PURCHASED_PRODUCTS";
    private final APIHelper apiHelper;
    private Set<String> purchasedOrderList = new HashSet<>();
    private SharedPreferences preferences;

    public HabiticaPurchaseVerifier(Context context, APIHelper apiHelper) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        preferences.getStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);

        this.apiHelper = apiHelper;
    }

    @Override
    protected void doVerify(@NonNull final List<Purchase> purchases, @NonNull final RequestListener<List<Purchase>> requestListener) {
        final List<Purchase> verifiedPurchases = new ArrayList<>(purchases.size());

        for (final Purchase purchase : purchases) {
            if (purchasedOrderList.contains(purchase.orderId)) {
                verifiedPurchases.add(purchase);

                requestListener.onSuccess(verifiedPurchases);
            } else {
                PurchaseValidationRequest validationRequest = new PurchaseValidationRequest();
                validationRequest.transaction = new Transaction();
                validationRequest.transaction.receipt = purchase.data;
                validationRequest.transaction.signature = purchase.signature;

                apiHelper.apiService.validatePurchase(validationRequest).subscribe(purchaseValidationResult -> {
                    purchasedOrderList.add(purchase.orderId);

                    verifiedPurchases.add(purchase);

                    requestListener.onSuccess(verifiedPurchases);
                }, throwable -> {
                    if (throwable.getClass().equals(HttpException.class)) {
                        HttpException error = (HttpException)throwable;
                        APIHelper.ErrorResponse res = apiHelper.getErrorResponse((HttpException) throwable);
                        if (error.code() == 401) {
                            if (res.message.equals("RECEIPT_ALREADY_USED")) {
                                purchasedOrderList.add(purchase.orderId);

                                verifiedPurchases.add(purchase);

                                requestListener.onSuccess(verifiedPurchases);
                                return;
                            }
                        }
                    }
                    requestListener.onError(purchases.indexOf(purchase), new Exception());
                });
            }
        }

        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);
        edit.apply();
    }
}
