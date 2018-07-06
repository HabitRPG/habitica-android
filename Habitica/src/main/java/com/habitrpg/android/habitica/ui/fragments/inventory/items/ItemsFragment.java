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

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

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
                        fragment.setItemType("eggs");
                        break;
                    }
                    case 1: {
                        fragment.setItemType("hatchingPotions");
                        break;
                    }
                    case 2: {
                        fragment.setItemType("food");
                        break;
                    }
                    case 3: {
                        fragment.setItemType("quests");
                        break;
                    }
                    case 4: {
                        fragment.setItemType("special");
                    }
                }
                fragment.setHatching(false);
                fragment.setFeeding(false);
                fragment.setItemTypeText(this.getPageTitle(position).toString());
                fragment.setUser(ItemsFragment.this.user);

                return fragment;
            }

            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (activity == null) {
                    return "";
                }
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
                        return activity.getString(R.string.special);
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
                fragment.setItemType("hatchingPotions");
                fragment.setHatchingItem(event.usingEgg);
            } else {
                fragment.setItemType("eggs");
                fragment.setHatchingItem(event.usingHatchingPotion);
            }
            fragment.setHatching(true);
            fragment.setFeeding(false);
            fragment.show(getFragmentManager(), "hatchingDialog");
        }
    }


    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.sidebar_items);
    }
}
