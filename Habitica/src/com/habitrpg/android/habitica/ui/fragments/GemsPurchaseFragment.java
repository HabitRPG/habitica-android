package com.habitrpg.android.habitica.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Negue on 24.11.2015.
 */
public class GemsPurchaseFragment extends BaseFragment {

    @InjectView(R.id.btn_purchase_gems)
    Button btnPurchaseGems;


    private ActivityCheckout checkout = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_gem_purchase, container, false);

        ButterKnife.inject(this, v);

        checkout = Checkout.forActivity(this.activity, HabiticaApplication.Instance.getCheckout());

        checkout.start();
        // you only need this if this activity starts purchase process
        checkout.createPurchaseFlow(new RequestListener<Purchase>() {
            @Override
            public void onSuccess(Purchase purchase) {

            }

            @Override
            public void onError(int i, Exception e) {

            }
        });


        checkout.whenReady(new Checkout.Listener() {
            @Override
            public void onReady(BillingRequests billingRequests) {
                billingRequests.purchase(ProductTypes.IN_APP, HabiticaApplication.Purchase20Gems, null, checkout.getPurchaseFlow());
            }

            @Override
            public void onReady(BillingRequests billingRequests, String s, boolean b) {

            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        checkout.stop();
        super.onDestroy();
    }
}
