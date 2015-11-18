package com.habitrpg.android.habitica;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.mikepenz.materialdrawer.Drawer;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GemPurchaseActivity extends AppCompatActivity {

    // region View Elements

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    Drawer drawer;

    // endregion

    // region IAP

    @NonNull
    private final ActivityCheckout checkout = Checkout.forActivity(this, HabiticaApplication.Instance.getCheckout());

    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gem_purchase);

        // Inject Controls
        ButterKnife.inject(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {

                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }

            actionBar.setTitle("Purchase Gems");
        }

        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar)
                .build();

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        checkout.stop();
        super.onDestroy();
    }
}
