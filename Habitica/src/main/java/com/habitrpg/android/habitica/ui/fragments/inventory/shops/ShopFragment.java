package com.habitrpg.android.habitica.ui.fragments.inventory.shops;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter;
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport;
import com.habitrpg.android.habitica.ui.menu.DividerItemDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShopFragment extends BaseFragment {
    private static final String SHOP_IDENTIFIER_KEY = "SHOP_IDENTIFIER_KEY";

    @BindView(R.id.recyclerView)
    public RecyclerViewEmptySupport recyclerView;
    @BindView(R.id.empty_view)
    public TextView emptyView;

    private View view;
    public ShopRecyclerAdapter adapter;

    public String shopIdentifier;
    public HabitRPGUser user;

    public Shop shop;

    @Inject
    APIHelper apiHeliper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        boolean setupViews = false;
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
            setupViews = true;
        }

        unbinder = ButterKnife.bind(this, view);

        if (setupViews) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }


        adapter = (ShopRecyclerAdapter) recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new ShopRecyclerAdapter();
            recyclerView.setAdapter(adapter);
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(getContext());

            recyclerView.setLayoutManager(layoutManager);
        }

        if (savedInstanceState != null) {
            this.shopIdentifier = savedInstanceState.getString(SHOP_IDENTIFIER_KEY, "");
        }

        if (shop == null) {
            loadShopInventory();
        } else {
            adapter.setShop(shop);
        }

        return view;
    }

    private void loadShopInventory() {
        String shopUrl = "";
        switch (this.shopIdentifier) {
            case Shop.MARKET:
                shopUrl = "market";
                break;
            case Shop.QUEST_SHOP:
                shopUrl = "quests";
                break;
            case Shop.TIME_TRAVELERS_SHOP:
                shopUrl = "time-travelers";
                break;
            case Shop.SEASONAL_SHOP:
                shopUrl = "seasonal";
                break;
        }
        this.apiHeliper.apiService.fetchShopInventory(shopUrl)
                .compose(this.apiHeliper.configureApiCallObserver())
                .subscribe(shop -> {
                    this.shop = shop;
                    this.adapter.setShop(shop);
                }, throwable -> {});
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SHOP_IDENTIFIER_KEY, this.shopIdentifier);
    }
}
