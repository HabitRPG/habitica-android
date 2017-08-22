package com.habitrpg.android.habitica.ui.fragments.inventory.shops;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopCategory;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport;
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

import static com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.showSnackbar;

public class ShopFragment extends BaseFragment {
    private static final String SHOP_IDENTIFIER_KEY = "SHOP_IDENTIFIER_KEY";

    @BindView(R.id.recyclerView)
    public RecyclerViewEmptySupport recyclerView;
    @BindView(R.id.empty_view)
    public TextView emptyView;
    public ShopRecyclerAdapter adapter;
    public String shopIdentifier;
    public User user;
    public Shop shop;
    @Inject
    InventoryRepository inventoryRepository;
    @Inject
    UserRepository userRepository;
    private View view;

    private GridLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        }

        unbinder = ButterKnife.bind(this, view);

        recyclerView.setBackgroundResource(R.color.white);

        adapter = (ShopRecyclerAdapter) recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new ShopRecyclerAdapter();
            recyclerView.setAdapter(adapter);
        }
        if (layoutManager == null) {
            layoutManager = new GridLayoutManager(getContext(), 2);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter.getItemViewType(position) < 2) {
                        return layoutManager.getSpanCount();
                    } else {
                        return 1;
                    }
                }
            });
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

    @Override
    public void onDestroyView() {
        userRepository.close();
        inventoryRepository.close();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View finalView = view;
        finalView.post(() -> setGridSpanCount(finalView.getWidth()));
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
        this.inventoryRepository.fetchShopInventory(shopUrl)
                .map(shop1 -> {
                    if (shop1.identifier.equals(Shop.MARKET)) {
                        if (user != null && user.isValid() && user.getPurchased().getPlan().isActive()) {
                            ShopCategory specialCategory = new ShopCategory();
                            specialCategory.text = getString(R.string.special);
                            specialCategory.items = new ArrayList<>();
                            ShopItem item = ShopItem.makeGemItem(getContext().getResources());
                            item.limitedNumberLeft = user.getPurchased().getPlan().numberOfGemsLeft();
                            specialCategory.items.add(item);
                            shop1.categories.add(specialCategory);
                        }
                    }
                    return shop1;
                })
                .subscribe(shop -> {
                    this.shop = shop;
                    this.adapter.setShop(shop);
                }, RxErrorHandler.handleEmptyError());
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

    private void setGridSpanCount(int width) {
        float itemWidth;
        itemWidth = getContext().getResources().getDimension(R.dimen.shopitem_width);

        int spanCount = (int) (width / itemWidth);
        if (spanCount == 0) {
            spanCount = 1;
        }
        layoutManager.setSpanCount(spanCount);
        layoutManager.requestLayout();
    }

}
