package com.habitrpg.android.habitica.ui.fragments;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;

import org.greenrobot.eventbus.EventBus;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Purchases;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

public class GemsPurchaseFragment extends BaseMainFragment {

    private static final int GEMS_TO_ADD = 21;

    private Listener listener;

    private BillingRequests billingRequests;

    @BindView(R.id.btn_purchase_gems)
    Button btnPurchaseGems;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (Listener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_gem_purchase, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnPurchaseGems.setEnabled(false);
        ViewHelper.SetBackgroundTint(btnPurchaseGems, ContextCompat.getColor(getContext(), R.color.brand));

        final ActivityCheckout checkout = listener.getActivityCheckout();

        checkout.destroyPurchaseFlow();

        checkout.createPurchaseFlow(new RequestListener<Purchase>() {
            @Override
            public void onSuccess(@NonNull Purchase purchase) {
                if (purchase.sku.equals(HabiticaApplication.Purchase20Gems)) {
                    billingRequests.consume(purchase.token, new RequestListener<Object>() {
                        @Override
                        public void onSuccess(@NonNull Object o) {
                            EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                        }

                        @Override
                        public void onError(int i, @NonNull Exception e) {
                            Fabric.getLogger().e("Purchase", "Consume", e);
                        }
                    });
                }
            }

            @Override
            public void onError(int i, @NonNull Exception e) {
                Fabric.getLogger().e("Purchase", "Error", e);
            }
        });


        checkout.whenReady(new Checkout.Listener() {
            @Override
            public void onReady(@NonNull final BillingRequests billingRequests) {
                GemsPurchaseFragment.this.billingRequests = billingRequests;

                // if the user leaves the fragment before the checkout callback is done
                if(btnPurchaseGems != null) {
                    btnPurchaseGems.setEnabled(true);

                }
                checkIfPendingPurchases();
            }

            @Override
            public void onReady(@NonNull BillingRequests billingRequests, @NonNull String s, boolean b) {

                checkout.loadInventory().whenLoaded(products -> {

                    Inventory.Product gems = products.get(ProductTypes.IN_APP);

                    java.util.List<Sku> skus = gems.getSkus();

                    for (Sku sku : skus){
                        updateBuyButtonText(sku.price);
                    }
                });

            }
        });
    }

    private void updateBuyButtonText(String price){
        if(price == null || price.isEmpty()){
            btnPurchaseGems.setText("+"+ GEMS_TO_ADD);
        }
        else
        {
            btnPurchaseGems.setText(price + " = " +"+"+GEMS_TO_ADD );
        }
    }

    private void checkIfPendingPurchases(){
        billingRequests.getAllPurchases(ProductTypes.IN_APP, new RequestListener<Purchases>() {
            @Override
            public void onSuccess(@NonNull Purchases purchases) {
                for(Purchase purchase : purchases.list){
                    if(purchase.sku.equals(HabiticaApplication.Purchase20Gems)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(@NonNull Object o) {
                                EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                            }

                            @Override
                            public void onError(int i,@NonNull  Exception e) {
                                Fabric.getLogger().e("Purchase", "Consume", e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(int i,@NonNull  Exception e) {
                Fabric.getLogger().e("Purchase","getAllPurchases", e);
            }
        });
    }

    @OnClick(R.id.btn_purchase_gems)
    public void doPurchaseGems(Button button) {
        // check if the user already bought and if it hasn't validated yet
        billingRequests.isPurchased(ProductTypes.IN_APP, HabiticaApplication.Purchase20Gems, new RequestListener<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (!aBoolean) {
                    // no current product exist
                    final ActivityCheckout checkout = listener.getActivityCheckout();
                    billingRequests.purchase(ProductTypes.IN_APP, HabiticaApplication.Purchase20Gems, null, checkout.getPurchaseFlow());
                }
                else{
                    checkIfPendingPurchases();
                }
            }

            @Override
            public void onError(int i,@NonNull  Exception e) {
                Fabric.getLogger().e("Purchase", "Error", e);
            }
        });

    }

    public interface Listener {
        ActivityCheckout getActivityCheckout();
    }
}
