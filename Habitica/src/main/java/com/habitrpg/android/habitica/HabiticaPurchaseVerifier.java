package com.habitrpg.android.habitica;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Transaction;

import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Negue on 26.11.2015.
 */
public class HabiticaPurchaseVerifier extends BasePurchaseVerifier {

    private Set<String> purchasedOrderList = new HashSet<>();
    private static final String PURCHASED_PRODUCTS_KEY = "PURCHASED_PRODUCTS";
    private SharedPreferences preferences;

    public HabiticaPurchaseVerifier(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        preferences.getStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);
    }

    @Override
    protected void doVerify(final List<Purchase> purchases, final RequestListener<List<Purchase>> requestListener) {
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

                PurchaseValidationResult purchaseValidationResult = HabiticaApplication.ApiHelper.validatePurchase(validationRequest);

                if (purchaseValidationResult.ok) {
                    purchasedOrderList.add(purchase.orderId);

                    verifiedPurchases.add(purchase);

                    requestListener.onSuccess(verifiedPurchases);
                } else {
                    requestListener.onError(purchases.indexOf(purchase), new Exception(purchaseValidationResult.data.toString()));
                }
            }
        }

        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);
        edit.apply();
    }
}
