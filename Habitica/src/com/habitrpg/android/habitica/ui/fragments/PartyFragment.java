package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by admin on 18/11/15.
 */
public class PartyFragment extends BaseFragment {

    public ViewPager viewPager;
    private Group group;
    private HashMap<Integer, Fragment> fragmentDictionary = new HashMap<>();

    private PartyMemberListFragment partyMemberListFragment;
    private PartyInformationFragment partyInformationFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_party, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        viewPager.setCurrentItem(0);

        final ContentCache contentCache = new ContentCache(mAPIHelper.apiService);


        // Get the full group data
        mAPIHelper.apiService.getGroup("party", new Callback<Group>() {
            @Override
            public void success(Group group, Response response) {
                PartyFragment.this.group = group;

                if (partyMemberListFragment != null) {
                    partyMemberListFragment.setMemberList(group.members);
                }

                if (partyInformationFragment != null) {
                    partyInformationFragment.setGroup(group);
                }

                if (group != null && group.quest != null && !group.quest.key.isEmpty()) {
                    contentCache.GetQuestContent(group.quest.key, new ContentCache.QuestContentCallback() {
                        @Override
                        public void GotQuest(QuestContent content) {
                            if (partyInformationFragment != null) {
                                partyInformationFragment.setQuestContent(content);
                            }
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
        setViewPagerAdapter();

        return v;
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 0: {
                        fragment = partyInformationFragment = new PartyInformationFragment(group);
                        break;
                    }
                    case 1: {
                        fragment = new ChatListFragment(activity, "party", mAPIHelper, user, false);
                        break;
                    }
                    case 2: {
                        fragment = partyMemberListFragment = new PartyMemberListFragment(activity, group);
                        break;
                    }
                    default:
                        fragment = new Fragment();
                }

                fragmentDictionary.put(position, fragment);

                return fragment;
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Party";
                    case 1:
                        return "Chat";
                    case 2:
                        return "Members";
                }
                return "";
            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }
}
