package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by admin on 18/11/15.
 */
public class PartyFragment extends BaseMainFragment {

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
                if (group == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                            .setMessage(activity.getString(R.string.no_party_message))
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    activity.getSupportFragmentManager().popBackStackImmediate();
                                }
                            });
                    builder.show();
                    tabLayout.removeAllTabs();
                    return;
                }
                PartyFragment.this.group = group;

                if (partyMemberListFragment != null) {
                    partyMemberListFragment.setMemberList(group.members);
                }

                if (partyInformationFragment != null) {
                    partyInformationFragment.setGroup(group);
                }

                if (group.quest != null && group.quest.key != null && !group.quest.key.isEmpty()) {
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

        this.tutorialStepIdentifier = "party";
        this.tutorialText = getString(R.string.tutorial_party);

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
                        fragment = partyInformationFragment = PartyInformationFragment.newInstance(group);
                        break;
                    }
                    case 1: {
                        ChatListFragment chatListFragment = new ChatListFragment();
                        chatListFragment.configure(activity, "party", mAPIHelper, user, activity, false);
                        fragment = chatListFragment;
                        break;
                    }
                    case 2: {
                        PartyMemberListFragment memberFragment = new PartyMemberListFragment();
                        memberFragment.configure(activity, group);
                        fragment = memberFragment;
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
                        return activity.getString(R.string.party);
                    case 1:
                        return activity.getString(R.string.chat);
                    case 2:
                        return activity.getString(R.string.members);
                }
                return "";
            }
        });

        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }
}
