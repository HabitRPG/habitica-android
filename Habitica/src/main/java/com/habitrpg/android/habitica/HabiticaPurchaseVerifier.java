package com.habitrpg.android.habitica;

import com.habitrpg.android.habitica.events.UserSubscribedEvent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.magicmicky.habitrpgwrapper.lib.api.IApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.Transaction;
import com.playseeds.android.sdk.Seeds;

import org.greenrobot.eventbus.EventBus;
import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

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
    private final IApiClient apiClient;
    private Set<String> purchasedOrderList = new HashSet<>();
    private SharedPreferences preferences;

    public HabiticaPurchaseVerifier(Context context, IApiClient apiClient) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        preferences.getStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);

        this.apiClient = apiClient;
    }

    @Override
    protected void doVerify(@NonNull final List<Purchase> purchases, @NonNull final RequestListener<List<Purchase>> requestListener) {
        final List<Purchase> verifiedPurchases = new ArrayList<>(purchases.size());

        for (final Purchase purchase : purchases) {
            if (purchasedOrderList.contains(purchase.orderId)) {
                verifiedPurchases.add(purchase);

                requestListener.onSuccess(verifiedPurchases);
            } else {
                if (PurchaseTypes.allGemTypes.contains(purchase.sku)) {
                    PurchaseValidationRequest validationRequest = new PurchaseValidationRequest();
                    validationRequest.transaction = new Transaction();
                    validationRequest.transaction.receipt = purchase.data;
                    validationRequest.transaction.signature = purchase.signature;

                apiClient.validatePurchase(validationRequest).subscribe(purchaseValidationResult -> {
                    purchasedOrderList.add(purchase.orderId);

                        requestListener.onSuccess(verifiedPurchases);


                    //TODO: find way to get $ price automatically.
                    if (purchase.sku.equals(PurchaseTypes.Purchase4Gems)) {
                        Seeds.sharedInstance().recordIAPEvent(purchase.sku, 0.99);
                    } else if (purchase.sku.equals(PurchaseTypes.Purchase21Gems)) {
                        Seeds.sharedInstance().recordIAPEvent(purchase.sku, 4.99);
                    } else if (purchase.sku.equals(PurchaseTypes.Purchase42Gems)) {
                        Seeds.sharedInstance().recordIAPEvent(purchase.sku, 9.99);
                    } else if (purchase.sku.equals(PurchaseTypes.Purchase84Gems)) {
                        Seeds.sharedInstance().recordSeedsIAPEvent(purchase.sku, 19.99);
                    }
                }, throwable -> {
                    if (throwable.getClass().equals(HttpException.class)) {
                        HttpException error = (HttpException)throwable;
                        ApiClient.ErrorResponse res = apiClient.getErrorResponse((HttpException) throwable);
                        if (error.code() == 401) {
                            if (res.message.equals("RECEIPT_ALREADY_USED")) {
                                purchasedOrderList.add(purchase.orderId);

                                    requestListener.onSuccess(verifiedPurchases);
                                    return;
                                }
                            }
                        }
                        requestListener.onError(purchases.indexOf(purchase), new Exception());
                    });
                } else if (PurchaseTypes.allSubscriptionTypes.contains(purchase.sku)) {
                    SubscriptionValidationRequest validationRequest = new SubscriptionValidationRequest();
                    validationRequest.transaction = new Transaction();
                    validationRequest.transaction.receipt = purchase.data;
                    validationRequest.transaction.signature = purchase.signature;
                    validationRequest.sku = purchase.sku;
                    apiClient.validateSubscription(validationRequest).subscribe(purchaseValidationResult -> {
                        purchasedOrderList.add(purchase.orderId);

                        verifiedPurchases.add(purchase);

                        requestListener.onSuccess(verifiedPurchases);

                        EventBus.getDefault().post(new UserSubscribedEvent());
                    }, throwable -> {
                        if (throwable.getClass().equals(HttpException.class)) {
                            HttpException error = (HttpException) throwable;
                            ApiClient.ErrorResponse res = apiClient.getErrorResponse((HttpException) throwable);
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
        }

        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);
        edit.apply();
    }
}
