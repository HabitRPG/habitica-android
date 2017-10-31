package com.habitrpg.android.habitica.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;
import com.habitrpg.android.habitica.ui.fragments.SubscriptionFragment;
import com.playseeds.android.sdk.Seeds;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Purchases;
import org.solovyev.android.checkout.RequestListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class GemPurchaseActivity extends BaseActivity implements InAppMessageListener {

    @Inject
    CrashlyticsProxy crashlyticsProxy;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    List<CheckoutFragment> fragments = new ArrayList<>();
    private ActivityCheckout checkout;
    private BillingRequests billingRequests;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_gem_purchase;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
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

        viewPager.setCurrentItem(0);

        setViewPagerAdapter();

        checkout.destroyPurchaseFlow();

        checkout.createPurchaseFlow(new RequestListener<Purchase>() {
            @Override
            public void onSuccess(@NonNull Purchase purchase) {
                if (PurchaseTypes.allGemTypes.contains(purchase.sku)) {
                    billingRequests.consume(purchase.token, new RequestListener<Object>() {
                        @Override
                        public void onSuccess(@NonNull Object o) {
                            //EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                            if (purchase.sku.equals(PurchaseTypes.Purchase84Gems)) {
                                GemPurchaseActivity.this.showSeedsPromo(getString(R.string.seeds_interstitial_sharing), "store");
                            }
                        }

                        @Override
                        public void onError(int i, @NonNull Exception e) {
                            crashlyticsProxy.fabricLogE("Purchase", "Consume", e);
                        }
                    });
                }
            }

            @Override
            public void onError(int i, @NonNull Exception e) {
                crashlyticsProxy.fabricLogE("Purchase", "Error", e);
            }
        });


        checkout.whenReady(new Checkout.Listener() {
            @Override
            public void onReady(@NonNull final BillingRequests billingRequests) {
                GemPurchaseActivity.this.billingRequests = billingRequests;

                for (CheckoutFragment fragment : fragments) {
                    fragment.setBillingRequests(billingRequests);
                }

                checkIfPendingPurchases();
            }

            @Override
            public void onReady(@NonNull BillingRequests billingRequests, @NonNull String s, boolean b) {
            }
        });
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
        checkout = Checkout.forActivity(this, HabiticaApplication.getInstance(this).getBilling());
        checkout.start();
    }

    public ActivityCheckout getActivityCheckout() {
        return checkout;
    }

    @Override
    public void inAppMessageClicked(String messageId) {
        for (CheckoutFragment fragment : fragments) {
            if (fragment.getClass().isAssignableFrom(GemsPurchaseFragment.class)) {
                ((GemsPurchaseFragment)fragment).purchaseGems(PurchaseTypes.Purchase84Gems);
            }
        }
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

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                CheckoutFragment fragment;
                if (position == 0) {
                    fragment = new GemsPurchaseFragment();
                } else {
                    fragment = new SubscriptionFragment();
                }
                if (fragments.size() > position) {
                    fragments.set(position, fragment);
                } else {
                    fragments.add(fragment);
                }
                fragment.setListener(GemPurchaseActivity.this);
                fragment.setupCheckout();
                if (billingRequests != null) {
                    fragment.setBillingRequests(billingRequests);
                }
                return (Fragment) fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.gems);
                    case 1:
                        return getString(R.string.subscriptions);
                }
                return "";
            }
        });

        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    private void checkIfPendingPurchases() {
        billingRequests.getAllPurchases(ProductTypes.IN_APP, new RequestListener<Purchases>() {
            @Override
            public void onSuccess(@NonNull Purchases purchases) {
                for (Purchase purchase : purchases.list) {
                    if (PurchaseTypes.allGemTypes.contains(purchase.sku)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(@NonNull Object o) {
                                //EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
                            }

                            @Override
                            public void onError(int i, @NonNull Exception e) {
                                crashlyticsProxy.fabricLogE("Purchase", "Consume", e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(int i, @NonNull Exception e) {
                crashlyticsProxy.fabricLogE("Purchase", "getAllPurchases", e);
            }
        });
    }

    public interface CheckoutFragment {

        void setupCheckout();

        void setListener(GemPurchaseActivity listener);

        void setBillingRequests(BillingRequests billingRequests);
    }

}
