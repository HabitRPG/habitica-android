package com.habitrpg.android.habitica.ui.activities;


import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;
import com.playseeds.android.sdk.Seeds;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class GemPurchaseActivity extends BaseActivity implements GemsPurchaseFragment.Listener, InAppMessageListener {

    private ActivityCheckout checkout;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_gem_purchase;
    }

    @Override
    protected void injectActivity(AppComponent component) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupCheckout();

        Seeds.sharedInstance()
                .simpleInit(this, this, "https://dash.playseeds.com", getString(R.string.seeds_app_key))
                .setLoggingEnabled(true);
        Seeds.sharedInstance().requestInAppMessage(getString(R.string.seeds_interstitial_gems));
        Seeds.sharedInstance().requestInAppMessage(getString(R.string.seeds_interstitial_sharing));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.gem_purchase_toolbartitle);
        }

        GemsPurchaseFragment firstFragment = new GemsPurchaseFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();
    }

    @Override
    public void onDestroy() {
        if (checkout != null) {
            checkout.stop();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupCheckout() {
        checkout = Checkout.forActivity(this, HabiticaApplication.getInstance(this).getCheckout());
        checkout.start();
    }

    @Override
    public ActivityCheckout getActivityCheckout() {
        return checkout;
    }

    @Override
    public void inAppMessageClicked(String messageId) {
        GemsPurchaseFragment fragment = (GemsPurchaseFragment) getSupportFragmentManager().getFragments().get(0);
        fragment.purchaseGems(PurchaseTypes.Purchase84Gems);
    }

    @Override
    public void inAppMessageDismissed(String messageId) {

    }

    @Override
    public void inAppMessageLoadSucceeded(String messageId) {

    }

    @Override
    public void inAppMessageShown(String messageId, boolean succeeded) {

    }

    @Override
    public void noInAppMessageFound(String messageId) {

    }

    @Override
    public void inAppMessageClickedWithDynamicPrice(String messageId, Double price) {

    }

    public void showSeedsPromo(final String messageId, final String context) {
        try {
            runOnUiThread(() -> {
                if (Seeds.sharedInstance().isInAppMessageLoaded(messageId)) {
                    Seeds.sharedInstance().showInAppMessage(messageId, context);
                } else {
                    // Skip the interstitial showing this time and try to reload the interstitial
                    Seeds.sharedInstance().requestInAppMessage(messageId);
                }
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}
