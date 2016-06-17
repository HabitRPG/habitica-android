package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TavernFragment extends BaseMainFragment {

    public ViewPager viewPager;
    Group tavern;

    ChatListFragment chatListFragment;
    GroupInformationFragment questInfoFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        viewPager.setCurrentItem(0);

        setViewPagerAdapter();

        this.tutorialStepIdentifier = "tavern";
        this.tutorialText = getString(R.string.tutorial_tavern);

        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.apiHelper != null) {
            apiHelper.apiService.getGroup("habitrpg")
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(group -> {
                TavernFragment.this.tavern = group;
                if (group.quest != null && group.quest.key != null && TavernFragment.this.isAdded()) {
                    TavernFragment.this.viewPager.getAdapter().notifyDataSetChanged();
                    if (TavernFragment.this.tabLayout != null) {
                        TavernFragment.this.tabLayout.setVisibility(View.VISIBLE);
                        TavernFragment.this.tabLayout.setupWithViewPager(TavernFragment.this.viewPager);
                    }

                    ContentCache contentCache = new ContentCache(apiHelper.apiService);

                    contentCache.GetQuestContent(group.quest.key, content -> {
                        if (questInfoFragment != null) {
                            questInfoFragment.setQuestContent(content);
                        }
                    });
                }
            }, throwable -> {});
        }
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        if (this.user == null) {
            return;
        }

        this.viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 0: {
                        chatListFragment = new ChatListFragment();
                        chatListFragment.configure("habitrpg", user, true);
                        fragment = chatListFragment;
                        break;
                    }
                    case 1: {
                        fragment = questInfoFragment = GroupInformationFragment.newInstance(tavern, user);
                        break;
                    }
                    default:
                        fragment = new Fragment();
                }

                return fragment;
            }

            @Override
            public int getCount() {
                if (tavern != null && tavern.quest != null && tavern.quest.key != null) {
                    return 2;
                }
                return 1;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return activity.getString(R.string.chat);
                    case 1:
                        return activity.getString(R.string.world_quest);
                }
                return "";
            }
        });

        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }
}
