package com.habitrpg.android.habitica.ui.activities;


import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class GemPurchaseActivity extends BaseActivity implements GemsPurchaseFragment.Listener {

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
}
