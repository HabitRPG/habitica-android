package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity;
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.UserParty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PartyFragment extends BaseMainFragment {

    public ViewPager viewPager;
    private Group group;

    private PartyMemberListFragment partyMemberListFragment;
    private GroupInformationFragment groupInformationFragment;
    private ChatListFragment chatListFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

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
                            .setNeutralButton(android.R.string.ok, (dialog, which) -> {
                                activity.getSupportFragmentManager().popBackStackImmediate();
                            });
                    builder.show();
                    tabLayout.removeAllTabs();
                    return;
                }
                PartyFragment.this.group = group;

                if (partyMemberListFragment != null) {
                    partyMemberListFragment.setMemberList(group.members);
                }

                if (groupInformationFragment != null) {
                    groupInformationFragment.setGroup(group);
                }

                if(chatListFragment != null){
                    chatListFragment.seenGroupId = group.id;
                }

                PartyFragment.this.activity.supportInvalidateOptionsMenu();

                if (group.quest != null && group.quest.key != null && !group.quest.key.isEmpty()) {
                    contentCache.GetQuestContent(group.quest.key, content -> {
                        if (groupInformationFragment != null) {
                            groupInformationFragment.setQuestContent(content);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.group != null) {
            if (this.group.leaderID.equals(this.user.getId())) {
                inflater.inflate(R.menu.menu_party_admin, menu);
            } else {
                inflater.inflate(R.menu.menu_party, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_invite_item:
                Intent intent = new Intent(getActivity(), PartyInviteActivity.class);
                startActivityForResult(intent, PartyInviteActivity.RESULT_SEND_INVITES);
                return true;
            case R.id.menu_guild_edit:
                this.displayEditForm();
                return true;
            case R.id.menu_guild_leave:
                this.mAPIHelper.apiService.leaveGroup(this.group.id, new Callback<Void>() {
                    @Override
                    public void success(Void aVoid, Response response) {
                        getActivity().getSupportFragmentManager().beginTransaction().remove(PartyFragment.this).commit();
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayEditForm() {
        Bundle bundle = new Bundle();
        bundle.putString("groupID",this.group.id);
        bundle.putString("name",this.group.name);
        bundle.putString("description",this.group.description);
        bundle.putString("leader",this.group.leaderID);

        Intent intent = new Intent(activity, GroupFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, GroupFormActivity.GROUP_FORM_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (GroupFormActivity.GROUP_FORM_ACTIVITY) : {
                if (resultCode == Activity.RESULT_OK) {
                    boolean needsSaving = false;
                    Bundle bundle = data.getExtras();
                    if (this.group.name != null && !this.group.name.equals(bundle.getString("name"))) {
                        this.group.name = bundle.getString("name");
                        needsSaving = true;
                    }
                    if (this.group.description != null && !this.group.description.equals(bundle.getString("description"))) {
                        this.group.description = bundle.getString("description");
                        needsSaving = true;
                    }
                    if (this.group.leaderID != null && !this.group.leaderID.equals(bundle.getString("leader"))) {
                        this.group.leaderID = bundle.getString("leader");
                        needsSaving = true;
                    }
                    if (this.group.privacy != null && !this.group.privacy.equals(bundle.getString("privacy"))) {
                        this.group.privacy = bundle.getString("privacy");
                        needsSaving = true;
                    }
                    if (needsSaving) {
                        this.mAPIHelper.apiService.updateGroup(this.group.id, this.group, new Callback<Void>() {
                            @Override
                            public void success(Void aVoid, Response response) {
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                        this.groupInformationFragment.setGroup(group);
                    }
                }
                break;
            }
            case (PartyInviteActivity.RESULT_SEND_INVITES) : {
                Map<String, Object> inviteData = new HashMap<>();
                inviteData.put("inviter", this.user.getProfile().getName());
                if (data.getBooleanExtra("isEmail", false)) {
                    String[] emails = data.getStringArrayExtra("emails");
                    List<HashMap<String, String>> invites = new ArrayList<>();
                    for (String email : emails) {
                        HashMap<String, String> invite = new HashMap<>();
                        invite.put("name", "");
                        invite.put("email", email);
                        invites.add(invite);
                    }
                    inviteData.put("emails", invites);
                } else {
                    String[] userIDs = data.getStringArrayExtra("userIDs");
                    List<String> invites = new ArrayList<>();
                    Collections.addAll(invites, userIDs);
                    inviteData.put("uuids", invites);
                }
                this.mAPIHelper.apiService.inviteToGroup(this.group.id, inviteData, new Callback<Void>() {
                    @Override
                    public void success(Void group, Response response) {
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }
        }
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        if (this.user == null) {
            return;
        }

        UserParty party = this.user.getParty();

        if(party == null) {
            return;
        }

        this.viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 0: {
                        fragment = groupInformationFragment = GroupInformationFragment.newInstance(group, user, mAPIHelper);
                        break;
                    }
                    case 1: {
                        chatListFragment = new ChatListFragment();
                        chatListFragment.configure(activity, "party", mAPIHelper, user, activity, false);
                        fragment = chatListFragment;
                        break;
                    }
                    case 2: {
                        partyMemberListFragment = new PartyMemberListFragment();
                        if (group != null) {
                            partyMemberListFragment.configure(activity, group.members);

                        } else {
                            partyMemberListFragment.configure(activity, null);
                        }
                        fragment = partyMemberListFragment;
                        break;
                    }
                    default:
                        fragment = new Fragment();
                }

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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && group != null) {
                    chatListFragment.setNavigatedToFragment(group.id);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1 && group != null) {
                    chatListFragment.setNavigatedToFragment(group.id);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }
}
