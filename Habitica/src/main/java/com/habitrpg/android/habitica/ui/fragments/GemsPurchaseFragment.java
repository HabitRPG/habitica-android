package com.habitrpg.android.habitica.ui.fragments;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView;
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.playseeds.android.sdk.Seeds;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;

public class GemsPurchaseFragment extends BaseFragment {

    @BindView(R.id.gems_4_view)
    GemPurchaseOptionsView gems4View;
    @BindView(R.id.gems_21_view)
    GemPurchaseOptionsView gems21View;
    @BindView(R.id.gems_42_view)
    GemPurchaseOptionsView gems42View;
    @BindView(R.id.gems_84_view)
    GemPurchaseOptionsView gems84View;

    @Inject
    CrashlyticsProxy crashlyticsProxy;

    private HashMap<String, String> priceMap;

    private static final int GEMS_TO_ADD = 21;
    Button btnPurchaseGems;
    private Listener listener;
    private BillingRequests billingRequests;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (Listener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        priceMap = new HashMap<>();

        return inflater.inflate(R.layout.fragment_gem_purchase, container, false);
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gems4View.setOnPurchaseClickListener(v -> purchaseGems(PurchaseTypes.Purchase4Gems));
        gems21View.setOnPurchaseClickListener(v -> purchaseGems(PurchaseTypes.Purchase21Gems));
        gems42View.setOnPurchaseClickListener(v -> purchaseGems(PurchaseTypes.Purchase42Gems));
        gems84View.setOnPurchaseClickListener(v -> purchaseGems(PurchaseTypes.Purchase84Gems));

        gems84View.seedsImageButton.setOnClickListener(v -> ((GemPurchaseActivity)this.getActivity()).showSeedsPromo(getString(R.string.seeds_interstitial_gems), "store"));

        final ActivityCheckout checkout = listener.getActivityCheckout();

        if (checkout != null) {
            checkout.destroyPurchaseFlow();

            checkout.createPurchaseFlow(new RequestListener<Purchase>() {
                @Override
                public void onSuccess(@NonNull Purchase purchase) {
                    if (PurchaseTypes.allTypes.contains(purchase.sku)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(@NonNull Object o) {
                                EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
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
                    GemsPurchaseFragment.this.billingRequests = billingRequests;

                    // if the user leaves the fragment before the checkout callback is done
                    if (btnPurchaseGems != null) {
                        btnPurchaseGems.setEnabled(true);

                    }
                    checkIfPendingPurchases();
                }

                @Override
                public void onReady(@NonNull BillingRequests billingRequests, @NonNull String s, boolean b) {

                    checkout.loadInventory().whenLoaded(products -> {

                        Inventory.Product gems = products.get(ProductTypes.IN_APP);

                        java.util.List<Sku> skus = gems.getSkus();

                        for (Sku sku : skus) {
                            priceMap.put(sku.id, sku.price);
                            updateButtonLabel(sku.id, sku.price);
                        }
                    });

                }
            });
        }
    }

    private void updateButtonLabel(String sku, String price) {
        GemPurchaseOptionsView matchingView;
        if (sku.equals(PurchaseTypes.Purchase4Gems)) {
            matchingView = gems4View;
        } else if (sku.equals(PurchaseTypes.Purchase21Gems)) {
            matchingView = gems21View;
        } else if (sku.equals(PurchaseTypes.Purchase42Gems)) {
            matchingView = gems42View;
        } else if (sku.equals(PurchaseTypes.Purchase84Gems)) {
            matchingView = gems84View;
        } else {
            return;
        }
        matchingView.setPurchaseButtonText(price);
        matchingView.setSku(sku);
    }

    private void checkIfPendingPurchases() {
        billingRequests.getAllPurchases(ProductTypes.IN_APP, new RequestListener<Purchases>() {
            @Override
            public void onSuccess(@NonNull Purchases purchases) {
                for (Purchase purchase : purchases.list) {
                    if (PurchaseTypes.allTypes.contains(purchase.sku)) {
                        billingRequests.consume(purchase.token, new RequestListener<Object>() {
                            @Override
                            public void onSuccess(@NonNull Object o) {
                                EventBus.getDefault().post(new BoughtGemsEvent(GEMS_TO_ADD));
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

    public void purchaseGems(String sku) {
        // check if the user already bought and if it hasn't validated yet
        billingRequests.isPurchased(ProductTypes.IN_APP, sku, new RequestListener<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (!aBoolean) {
                    // no current product exist
                    final ActivityCheckout checkout = listener.getActivityCheckout();
                    billingRequests.purchase(ProductTypes.IN_APP, sku, null, checkout.getPurchaseFlow());
                } else {
                    checkIfPendingPurchases();
                }
            }

            @Override
            public void onError(int i, @NonNull Exception e) {
                crashlyticsProxy.fabricLogE("Purchase", "Error", e);
            }
        });

    }

    public interface Listener {
        ActivityCheckout getActivityCheckout();
    }
}
