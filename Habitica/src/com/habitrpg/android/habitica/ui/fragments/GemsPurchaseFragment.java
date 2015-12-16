package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.MainActivity;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;

import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Purchases;
import org.solovyev.android.checkout.RequestListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Negue on 24.11.2015.
 */
public class GemsPurchaseFragment extends BaseFragment {

    private static final int GEMS_TO_ADD = 21;

    private BillingRequests billingRequests;

    @InjectView(R.id.btn_purchase_gems)
    Button btnPurchaseGems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_gem_purchase, container, false);

        ButterKnife.inject(this, v);

        btnPurchaseGems.setEnabled(false);
        ViewHelper.SetBackgroundTint(btnPurchaseGems, container.getResources().getColor(R.color.brand));

        MainActivity.checkout.destroyPurchaseFlow();

        MainActivity.checkout.createPurchaseFlow(new RequestListener<Purchase>() {
            @Override
            public void onSuccess(Purchase purchase) {
                if(purchase.sku.equals(HabiticaApplication.Purchase20Gems)){
                    billingRequests.consume(purchase.token, new RequestListener<Object>() {
                        @Override
                        public void onSuccess(Object o) {
                            EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                        }

                        @Override
                        public void onError(int i, Exception e) {
                            Fabric.getLogger().e("Purchase", "Consume", e);
                        }
                    });
                }
            }

            @Override
            public void onError(int i, Exception e) {
                Fabric.getLogger().e("Purchase", "Error", e);
            }
        });


        MainActivity.checkout.whenReady(new Checkout.Listener() {
            @Override
            public void onReady(final BillingRequests billingRequests) {
                GemsPurchaseFragment.this.billingRequests = billingRequests;

                btnPurchaseGems.setEnabled(true);

                checkIfPendingPurchases();
            }

            @Override
            public void onReady(BillingRequests billingRequests, String s, boolean b) {

            }
        });

        return v;
    }

    private void checkIfPendingPurchases(){
        billingRequests.getAllPurchases(ProductTypes.IN_APP, new RequestListener<Purchases>() {
            @Override
            public void onSuccess(Purchases purchases) {
                for(Purchase purchase : purchases.list){
                    if(purchase.sku.equals(HabiticaApplication.Purchase20Gems)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(Object o) {
                                EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                            }

                            @Override
                            public void onError(int i, Exception e) {
                                Fabric.getLogger().e("Purchase", "Consume", e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(int i, Exception e) {
                Fabric.getLogger().e("Purchase","getAllPurchases", e);
            }
        });
    }

    @OnClick(R.id.btn_purchase_gems)
    public void doPurchaseGems(Button button) {
        // check if the user already bought and if it hasn't validated yet
        billingRequests.isPurchased(ProductTypes.IN_APP, HabiticaApplication.Purchase20Gems, new RequestListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    // no current product exist
                    billingRequests.purchase(ProductTypes.IN_APP, HabiticaApplication.Purchase20Gems, null, MainActivity.checkout.getPurchaseFlow());
                }
                else{
                    checkIfPendingPurchases();
                }
            }

            @Override
            public void onError(int i, Exception e) {
                Fabric.getLogger().e("Purchase", "Error", e);
            }
        });

    }

}
