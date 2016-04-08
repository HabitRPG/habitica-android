package com.habitrpg.android.habitica.ui.fragments.social;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyMemberListFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.UserParty;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TavernFragment extends BaseMainFragment implements Callback<Group> {

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mAPIHelper != null) {
            mAPIHelper.apiService.getGroup("habitrpg", this);
        }
    }

    @Override
    public void success(Group group, Response response) {
        this.tavern = group;
        if (group.quest != null) {
            this.viewPager.getAdapter().notifyDataSetChanged();
            this.tabLayout.setVisibility(View.VISIBLE);
            this.tabLayout.setupWithViewPager(this.viewPager);

            ContentCache contentCache = new ContentCache(mAPIHelper.apiService);

            contentCache.GetQuestContent(group.quest.key, content -> {
                if (questInfoFragment != null) {
                    questInfoFragment.setQuestContent(content);
                }
            });
        }
    }

    @Override
    public void failure(RetrofitError error) {

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
                        chatListFragment.configure(activity, "habitrpg", mAPIHelper, user, activity, true);
                        fragment = chatListFragment;
                        break;
                    }
                    case 1: {
                        fragment = questInfoFragment = GroupInformationFragment.newInstance(tavern, user, mAPIHelper);
                        break;
                    }
                    default:
                        fragment = new Fragment();
                }

                return fragment;
            }

            @Override
            public int getCount() {
                if (tavern != null && tavern.quest != null) {
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

        tabLayout.setupWithViewPager(viewPager);
    }
}
