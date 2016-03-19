package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment;
import com.magicmicky.habitrpgwrapper.lib.models.UserParty;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ItemsFragment extends BaseMainFragment {

    @Bind(R.id.view_pager)
    public ViewPager viewPager;

    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_viewpager, container, false);

            ButterKnife.bind(this, view);

            viewPager.setCurrentItem(0);

            setViewPagerAdapter();
        }
        return view;
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        UserParty party = user.getParty();

        if (party == null) {
            return;
        }

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                ItemsRecyclerFragment fragment = new ItemsRecyclerFragment();
                switch (position) {
                    case 0:
                        fragment.itemType = "eggs";
                    case 1:
                        fragment.itemType = "hatchingpotions";
                    case 2:
                        fragment.itemType = "food";
                    case 3:
                        fragment.itemType = "quests";
                }
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
                        return activity.getString(R.string.eggs);
                    case 1:
                        return activity.getString(R.string.hatching_potions);
                    case 2:
                        return activity.getString(R.string.food);
                    case 3:
                        return activity.getString(R.string.quests);
                }
                return "";
            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }
}
