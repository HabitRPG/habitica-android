package com.habitrpg.android.habitica.ui.fragments.inventory.shops;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.views.CurrencyView;
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import rx.Observable;

import static com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.showSnackbar;

public class ShopsFragment extends BaseMainFragment {

    @Inject
    InventoryRepository inventoryRepository;

    public ViewPager viewPager;
    private CurrencyView currencyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        hideToolbar();
        disableToolbarScrolling();
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        viewPager.setCurrentItem(0);

        setViewPagerAdapter();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currencyView = new CurrencyView(getContext());
        if (toolbarAccessoryContainer != null) {
            toolbarAccessoryContainer.addView(currencyView);
        }
        updateCurrencyView();
    }

    @Override
    public void onDestroyView() {
        if (toolbarAccessoryContainer != null) {
            toolbarAccessoryContainer.removeView(currencyView);
        }
        showToolbar();
        enableToolbarScrolling();
        inventoryRepository.close();
        super.onDestroyView();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                ShopFragment fragment = new ShopFragment();

                switch (position) {
                    case 0: {
                        fragment.shopIdentifier = Shop.MARKET;
                        break;
                    }
                    case 1: {
                        fragment.shopIdentifier = Shop.QUEST_SHOP;
                        break;
                    }
                    case 2: {
                        fragment.shopIdentifier = Shop.TIME_TRAVELERS_SHOP;
                        break;
                    }
                    case 3: {
                        fragment.shopIdentifier = Shop.SEASONAL_SHOP;
                        break;
                    }
                }
                fragment.user = ShopsFragment.this.user;

                return fragment;
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getContext().getString(R.string.market);
                    case 1:
                        return getContext().getString(R.string.quests);
                    case 2:
                        return getContext().getString(R.string.timeTravelers);
                    case 3:
                        return getContext().getString(R.string.seasonalShop);
                }
                return "";
            }
        });

        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }


    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.sidebar_shops);
    }

    @Override
    public void updateUserData(User user) {
        super.updateUserData(user);
        updateCurrencyView();
    }

    private void updateCurrencyView() {
        if (user == null || currencyView == null) {
            return;
        }
        currencyView.setGold(user.getStats().getGp());
        currencyView.setGems(user.getGemCount());
        currencyView.setHourglasses(user.getHourglassCount());
    }

    @Subscribe
    public void onEvent(final BuyGemItemCommand event) {
        MainActivity activity = (MainActivity) getActivity();
        if (event.item.canBuy(user) || !event.item.getCurrency().equals("gems")) {
            Observable<Void> observable;
            if (event.shopIdentifier.equals(Shop.TIME_TRAVELERS_SHOP)) {
                if (event.item.purchaseType.equals("gear")) {
                    observable = inventoryRepository.purchaseMysterySet(event.item.categoryIdentifier);
                } else {
                    observable = inventoryRepository.purchaseHourglassItem(event.item.purchaseType, event.item.key);
                }
            } else if (event.item.purchaseType.equals("quests") && event.item.getCurrency().equals("gold")) {
                observable = inventoryRepository.purchaseQuest(event.item.key);
            } else {
                observable = inventoryRepository.purchaseItem(event.item.purchaseType, event.item.key);
            }
            observable
                    .doOnNext(aVoid -> showSnackbar(getContext(), activity.floatingMenuWrapper, getString(R.string.successful_purchase, event.item.text), HabiticaSnackbar.SnackbarDisplayType.SUCCESS))
                    .flatMap(buyResponse -> userRepository.retrieveUser(false))
                    .subscribe(buyResponse -> {}, throwable -> {
                        retrofit2.HttpException error = (retrofit2.HttpException) throwable;
                        if (error.code() == 401 && event.item.getCurrency().equals("gems")) {
                            activity.openGemPurchaseFragment(null);
                        }
                    });
        } else {
            activity.openGemPurchaseFragment(null);
        }
    }
}
