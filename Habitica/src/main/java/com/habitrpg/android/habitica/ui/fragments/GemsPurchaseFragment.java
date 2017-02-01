package com.habitrpg.android.habitica.ui.fragments;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.PurchaseTypes;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView;
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;

public class GemsPurchaseFragment extends BaseFragment implements GemPurchaseActivity.CheckoutFragment {

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

    private GemPurchaseActivity listener;
    private BillingRequests billingRequests;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

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
    }

    @Override
    public void setupCheckout() {
        final ActivityCheckout checkout = listener.getActivityCheckout();
        if (checkout != null) {
            Inventory inventory = checkout.makeInventory();

            inventory.load(Inventory.Request.create()
                            .loadAllPurchases().loadSkus(ProductTypes.IN_APP, PurchaseTypes.allGemTypes),
                    products -> {
                        Inventory.Product gems = products.get(ProductTypes.IN_APP);
                        if (!gems.supported) {
                            // billing is not supported, user can't purchase anything
                            return;
                        }
                        java.util.List<Sku> skus = gems.getSkus();
                        for (Sku sku : skus) {
                            updateButtonLabel(sku.id.code, sku.price);
                        }
                    });
        }
    }

    @Override
    public void setListener(GemPurchaseActivity listener) {
        this.listener = listener;
    }

    @Override
    public void setBillingRequests(BillingRequests billingRequests) {
        this.billingRequests = billingRequests;
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

    public void purchaseGems(String sku) {
        // check if the user already bought and if it hasn't validated yet
        billingRequests.isPurchased(ProductTypes.IN_APP, sku, new RequestListener<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (!aBoolean) {
                    // no current product exist
                    final ActivityCheckout checkout = listener.getActivityCheckout();
                    Log.d("GEM", sku);
                    billingRequests.purchase(ProductTypes.IN_APP, sku, null, checkout.getPurchaseFlow());
                }
            }

            @Override
            public void onError(int i, @NonNull Exception e) {
                crashlyticsProxy.fabricLogE("Purchase", "Error", e);
            }
        });

    }
}
