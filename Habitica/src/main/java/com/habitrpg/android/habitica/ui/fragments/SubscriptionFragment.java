package com.habitrpg.android.habitica.ui.fragments;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.SubscriptionOptionView;
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity;

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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;

public class SubscriptionFragment extends BaseFragment implements GemPurchaseActivity.CheckoutFragment {

    @Inject
    CrashlyticsProxy crashlyticsProxy;

    @BindView(R.id.subscription1month)
    SubscriptionOptionView subscription1MonthView;
    @BindView(R.id.subscription3month)
    SubscriptionOptionView subscription3MonthView;
    @BindView(R.id.subscription6month)
    SubscriptionOptionView subscription6MonthView;
    @BindView(R.id.subscription12month)
    SubscriptionOptionView subscription12MonthView;

    @BindView(R.id.subscribeButton)
    Button subscriptionButton;

    @Nullable
    String selectedSubscriptionSku;

    private GemPurchaseActivity listener;
    private BillingRequests billingRequests;
    private Inventory inventory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.subscription1MonthView.setOnPurchaseClickListener(view1 -> selectSubscription(PurchaseTypes.Subscription1Month));
        this.subscription3MonthView.setOnPurchaseClickListener(view1 -> selectSubscription(PurchaseTypes.Subscription3Month));
        this.subscription6MonthView.setOnPurchaseClickListener(view1 -> selectSubscription(PurchaseTypes.Subscription6Month));
        this.subscription12MonthView.setOnPurchaseClickListener(view1 -> selectSubscription(PurchaseTypes.Subscription12Month));
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void setupCheckout() {
        final ActivityCheckout checkout = listener.getActivityCheckout();
        if (checkout != null) {
            inventory = checkout.makeInventory();

            checkout.destroyPurchaseFlow();

            checkout.createPurchaseFlow(new RequestListener<Purchase>() {
                @Override
                public void onSuccess(@NonNull Purchase purchase) {
                    if (PurchaseTypes.allSubscriptionTypes.contains(purchase.sku)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(@NonNull Object o) {
                                if (purchase.sku.equals(PurchaseTypes.Purchase84Gems)) {
                                    ((GemPurchaseActivity)getActivity()).showSeedsPromo(getString(R.string.seeds_interstitial_sharing), "store");
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
                    SubscriptionFragment.this.billingRequests = billingRequests;

                    checkIfPendingPurchases();
                }

                @Override
                public void onReady(@NonNull BillingRequests billingRequests, @NonNull String s, boolean b) {
                    inventory.load(Inventory.Request.create()
                                    .loadAllPurchases().loadSkus(ProductTypes.SUBSCRIPTION, PurchaseTypes.allSubscriptionTypes),
                            products -> {
                                Inventory.Product subscriptions = products.get(ProductTypes.SUBSCRIPTION);

                                java.util.List<Sku> skus = subscriptions.getSkus();

                                for (Sku sku : skus) {
                                    updateButtonLabel(sku, sku.price, subscriptions);
                                }
                                selectSubscription(PurchaseTypes.Subscription1Month);
                            });
                }
            });
        }
    }

    private void updateButtonLabel(Sku sku, String price, Inventory.Product subscriptions) {
        SubscriptionOptionView matchingView = buttonForSku(sku);
        if (matchingView != null) {
            matchingView.setPriceText(price);
            matchingView.setSku(sku.id.code);
            matchingView.setIsPurchased(subscriptions.isPurchased(sku));
        }
    }

    private void selectSubscription(String sku) {
        if (this.selectedSubscriptionSku != null) {
            SubscriptionOptionView oldButton = buttonForSku(this.selectedSubscriptionSku);
            if (oldButton != null) {
                oldButton.setIsPurchased(false);
            }
        }
        this.selectedSubscriptionSku = sku;
        SubscriptionOptionView subscriptionOptionButton = buttonForSku(this.selectedSubscriptionSku);
        if (subscriptionOptionButton != null) {
            subscriptionOptionButton.setIsPurchased(true);
        }
        this.subscriptionButton.setEnabled(true);
    }

    @Nullable
    private SubscriptionOptionView buttonForSku(Sku sku) {
        return buttonForSku(sku.id.code);
    }

    @Nullable
    private SubscriptionOptionView buttonForSku(String sku) {
        if (sku.equals(PurchaseTypes.Subscription1Month)) {
            return subscription1MonthView;
        } else if (sku.equals(PurchaseTypes.Subscription3Month)) {
            return subscription3MonthView;
        } else if (sku.equals(PurchaseTypes.Subscription6Month)) {
            return subscription6MonthView;
        } else if (sku.equals(PurchaseTypes.Subscription12Month)) {
            return subscription12MonthView;
        } else {
            return null;
        }
    }

    @Override
    public void setListener(GemPurchaseActivity listener) {
        this.listener = listener;
    }

    private void checkIfPendingPurchases() {
        billingRequests.getAllPurchases(ProductTypes.SUBSCRIPTION, new RequestListener<Purchases>() {
            @Override
            public void onSuccess(@NonNull Purchases purchases) {
                for (Purchase purchase : purchases.list) {
                    if (PurchaseTypes.allGemTypes.contains(purchase.sku)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(@NonNull Object o) {
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

}
