package com.habitrpg.android.habitica.ui.fragments;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.SubscriptionDetailsView;
import com.habitrpg.android.habitica.ui.SubscriptionOptionView;
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionPlan;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;

public class SubscriptionFragment extends BaseFragment implements GemPurchaseActivity.CheckoutFragment {

    @Inject
    CrashlyticsProxy crashlyticsProxy;

    @Inject
    APIHelper apiHelper;

    @BindView(R.id.loadingIndicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.subscriptionOptions)
    View subscriptionOptions;

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

    @BindView(R.id.subscriptionDetails)
    SubscriptionDetailsView subscriptionDetailsView;

    @BindView(R.id.subscribeBenefitsTitle)
    TextView subscribeBenefitsTitle;

    @Nullable
    String selectedSubscriptionSku;

    private GemPurchaseActivity listener;
    private BillingRequests billingRequests;
    private Inventory inventory;

    private HabitRPGUser user;
    private boolean hasLoadedSubscriptionOptions;
    private boolean isSubscribed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        apiHelper.apiService.getUser().compose(apiHelper.configureApiCallObserver())
                .subscribe(this::setUser, throwable -> {});

        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscriptionOptions.setVisibility(View.GONE);
        subscriptionDetailsView.setVisibility(View.GONE);

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
                                hasLoadedSubscriptionOptions = true;
                                updateSubscriptionInfo();
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
                    if (PurchaseTypes.allSubscriptionTypes.contains(purchase.sku)) {
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

    public void setUser(HabitRPGUser newUser) {
        user = newUser;
        this.updateSubscriptionInfo();
    }

    private void updateSubscriptionInfo() {
        if (user != null) {
            SubscriptionPlan plan = user.getPurchased().getPlan();
            isSubscribed = false;
            if (plan != null) {
                if (plan.isActive()) {
                    isSubscribed = true;
                }
            }

            if (isSubscribed) {
                this.subscriptionDetailsView.setVisibility(View.VISIBLE);
                this.subscriptionDetailsView.setPlan(plan);
                this.subscribeBenefitsTitle.setText(R.string.subscribe_prompt_thanks);
            } else {
                if (!hasLoadedSubscriptionOptions) {
                    return;
                }
                this.subscriptionOptions.setVisibility(View.VISIBLE);
            }
            this.loadingIndicator.setVisibility(View.GONE);
        }
    }
}
