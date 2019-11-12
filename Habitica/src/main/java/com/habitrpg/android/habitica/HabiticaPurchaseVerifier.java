package com.habitrpg.android.habitica;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.events.ConsumablePurchasedEvent;
import com.habitrpg.android.habitica.events.UserSubscribedEvent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.models.IAPGift;
import com.habitrpg.android.habitica.models.PurchaseValidationRequest;
import com.habitrpg.android.habitica.models.SubscriptionValidationRequest;
import com.habitrpg.android.habitica.models.Transaction;
import com.habitrpg.android.habitica.models.responses.ErrorResponse;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.ResponseCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.HttpException;

/**
 * Created by Negue on 26.11.2015.
 */
public class HabiticaPurchaseVerifier extends BasePurchaseVerifier {

    private static final String PURCHASED_PRODUCTS_KEY = "PURCHASED_PRODUCTS";
    private static final String PENDING_GIFTS_KEY = "PENDING_GIFTS";
    private final ApiClient apiClient;
    private Set<String> purchasedOrderList = new HashSet<>();
    public static Map<String, String> pendingGifts = new HashMap<>();
    private SharedPreferences preferences;
    private Context context;

    public HabiticaPurchaseVerifier(Context context, ApiClient apiClient) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;

        preferences.getStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);
        pendingGifts = loadPendingGifts();

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
                    validationRequest.setSku(purchase.sku);
                    validationRequest.setTransaction(new Transaction());
                    validationRequest.getTransaction().setReceipt(purchase.data);
                    validationRequest.getTransaction().setSignature(purchase.signature);
                    if (pendingGifts.containsKey(purchase.sku)) {
                        validationRequest.setGift(new IAPGift());
                        validationRequest.getGift().uuid = pendingGifts.get(purchase.sku);
                        pendingGifts.remove(purchase.sku);
                    }

                    apiClient.validatePurchase(validationRequest).subscribe(purchaseValidationResult -> {
                        purchasedOrderList.add(purchase.orderId);

                        requestListener.onSuccess(verifiedPurchases);
                        EventBus.getDefault().post(new ConsumablePurchasedEvent(purchase));
                    }, throwable -> {
                        if (throwable.getClass().equals(retrofit2.adapter.rxjava2.HttpException.class)) {
                            HttpException error = (HttpException) throwable;
                            ErrorResponse res = apiClient.getErrorResponse((HttpException) throwable);
                            if (error.code() == 401) {
                                if (res.message != null && res.message.equals("RECEIPT_ALREADY_USED")) {
                                    purchasedOrderList.add(purchase.orderId);

                                    requestListener.onSuccess(verifiedPurchases);
                                    EventBus.getDefault().post(new ConsumablePurchasedEvent(purchase));
                                    return;
                                }
                            }
                        }
                        requestListener.onError(ResponseCodes.ERROR, new Exception());
                    });
                } else if (PurchaseTypes.allSubscriptionNoRenewTypes.contains(purchase.sku)) {
                    PurchaseValidationRequest validationRequest = new PurchaseValidationRequest();
                    validationRequest.setSku(purchase.sku);
                    validationRequest.setTransaction(new Transaction());
                    validationRequest.getTransaction().setReceipt(purchase.data);
                    validationRequest.getTransaction().setSignature(purchase.signature);
                    if (pendingGifts.containsKey(purchase.sku)) {
                        validationRequest.setGift(new IAPGift());
                        validationRequest.getGift().uuid = pendingGifts.get(purchase.sku);
                        pendingGifts.remove(purchase.sku);
                    }

                    apiClient.validateNoRenewSubscription(validationRequest).subscribe(purchaseValidationResult -> {
                        purchasedOrderList.add(purchase.orderId);
                        pendingGifts.remove(purchase.sku);
                        requestListener.onSuccess(verifiedPurchases);
                        EventBus.getDefault().post(new ConsumablePurchasedEvent(purchase));
                    }, throwable -> {
                        if (throwable.getClass().equals(retrofit2.adapter.rxjava2.HttpException.class)) {
                            HttpException error = (HttpException)throwable;
                            ErrorResponse res = apiClient.getErrorResponse((HttpException) throwable);
                            if (error.code() == 401) {
                                if (res.message != null && res.message.equals("RECEIPT_ALREADY_USED")) {
                                    purchasedOrderList.add(purchase.orderId);

                                    requestListener.onSuccess(verifiedPurchases);
                                    EventBus.getDefault().post(new ConsumablePurchasedEvent(purchase));
                                    return;
                                }
                            }
                        }
                        requestListener.onError(ResponseCodes.ERROR, new Exception());
                    });
                } else if (PurchaseTypes.allSubscriptionTypes.contains(purchase.sku)) {
                    SubscriptionValidationRequest validationRequest = new SubscriptionValidationRequest();
                    validationRequest.setSku(purchase.sku);
                    validationRequest.setTransaction(new Transaction());
                    validationRequest.getTransaction().setReceipt(purchase.data);
                    validationRequest.getTransaction().setSignature(purchase.signature);
                    apiClient.validateSubscription(validationRequest).subscribe(purchaseValidationResult -> {
                        purchasedOrderList.add(purchase.orderId);

                        verifiedPurchases.add(purchase);

                        requestListener.onSuccess(verifiedPurchases);
                        FirebaseAnalytics.getInstance(context).logEvent("user_subscribed", null);
                        EventBus.getDefault().post(new UserSubscribedEvent());
                    }, throwable -> {
                        if (throwable.getClass().equals(retrofit2.adapter.rxjava2.HttpException.class)) {
                            HttpException error = (HttpException) throwable;
                            ErrorResponse res = apiClient.getErrorResponse((HttpException) throwable);
                            if (error.code() == 401) {
                                if (res.message != null && res.message.equals("RECEIPT_ALREADY_USED")) {
                                    purchasedOrderList.add(purchase.orderId);

                                    verifiedPurchases.add(purchase);

                                    requestListener.onSuccess(verifiedPurchases);
                                    return;
                                }
                            }
                        }
                        requestListener.onError(ResponseCodes.ERROR, new Exception());
                    });
                }
            }
        }

        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet(PURCHASED_PRODUCTS_KEY, purchasedOrderList);
        edit.apply();

        savePendingGifts();
    }

    private void savePendingGifts(){
        JSONObject jsonObject = new JSONObject(pendingGifts);
        String jsonString = jsonObject.toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(PENDING_GIFTS_KEY).apply();
        editor.putString(PENDING_GIFTS_KEY, jsonString);
        editor.commit();
    }

    private Map<String, String> loadPendingGifts() {
        Map<String, String> outputMap = new HashMap<>();
        try{
                String jsonString = preferences.getString(PENDING_GIFTS_KEY, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    outputMap.put(key, value);
                }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }
}
