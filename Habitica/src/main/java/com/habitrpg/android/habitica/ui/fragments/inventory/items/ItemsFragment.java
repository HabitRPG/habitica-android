package com.habitrpg.android.habitica.ui.fragments.inventory.items;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.HatchingCommand;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;

import org.greenrobot.eventbus.Subscribe;

public class ItemsFragment extends BaseMainFragment {

    public ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        viewPager.setCurrentItem(0);

        setViewPagerAdapter();

        return v;
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

                ItemRecyclerFragment fragment = new ItemRecyclerFragment();

                switch (position) {
                    case 0: {
                        fragment.itemType = "eggs";
                        break;
                    }
                    case 1: {
                        fragment.itemType = "hatchingPotions";
                        break;
                    }
                    case 2: {
                        fragment.itemType = "food";
                        break;
                    }
                    case 3: {
                        fragment.itemType = "quests";
                        break;
                    }
                    case 4: {
                        fragment.itemType = "special";
                    }
                }
                fragment.isHatching = false;
                fragment.isFeeding = false;
                fragment.itemTypeText = this.getPageTitle(position).toString();
                fragment.user = ItemsFragment.this.user;

                return fragment;
            }

            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return activity.getString(R.string.eggs);
                    case 1:
                        return activity.getString(R.string.hatching_potions);
                    case 2:
                        return activity.getString(R.string.food);
                    case 3:
                        return activity.getString(R.string.quests);
                    case 4:
                        return getString(R.string.special);
                }
                return "";
            }
        });
        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
    }

    @Subscribe
    public void showHatchingDialog(HatchingCommand event) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            ItemRecyclerFragment fragment = new ItemRecyclerFragment();
            if (event.usingEgg != null) {
                fragment.itemType = "hatchingPotions";
                fragment.hatchingItem = event.usingEgg;
            } else {
                fragment.itemType = "eggs";
                fragment.hatchingItem = event.usingHatchingPotion;
            }
            fragment.isHatching = true;
            fragment.isFeeding = false;
            fragment.show(getFragmentManager(), "hatchingDialog");
        }
    }


    @Override
    public String customTitle() {
        if (isAdded()) {
            return getString(R.string.sidebar_items);
        } else {
            return "";
        }
    }
}
