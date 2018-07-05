package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity;
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class PartyFragment extends BaseMainFragment {

    @Inject
    SocialRepository socialRepository;

    public ViewPager viewPager;
    @Inject
    InventoryRepository inventoryRepository;
    @Nullable
    private Group group;
    private PartyMemberListFragment partyMemberListFragment;
    private ChatListFragment chatListFragment;
    private FragmentPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        hideToolbar();
        disableToolbarScrolling();
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

        viewPager.setCurrentItem(0);

        // Get the full group data
        if (userHasParty()) {
            if (user != null) {
                getCompositeSubscription().add(socialRepository.getGroup(user.getParty().getId())
                        .firstElement()
                        //delay, so that realm can save party first
                        .delay(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(group -> {
                            PartyFragment.this.group = group;
                            updateGroupUI();
                        }, RxErrorHandler.handleEmptyError()));
            }
            socialRepository.retrieveGroup("party")
                    .flatMap(group1 -> socialRepository.retrieveGroupMembers(group1.getId(), true))
                    .subscribe(members -> {}, RxErrorHandler.handleEmptyError());
        }

        setViewPagerAdapter();
        this.setTutorialStepIdentifier("party");
        this.setTutorialText(getString(R.string.tutorial_party));

        return v;
    }

    private boolean userHasParty() {
        return this.user != null && this.user.getParty() != null && this.user.getParty().id != null;
    }

    @Override
    public void onDestroyView() {
        showToolbar();
        enableToolbarScrolling();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        socialRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void updateGroupUI() {
        if (viewPagerAdapter != null) {
            viewPagerAdapter.notifyDataSetChanged();
        }

        if (tabLayout != null) {
            if (group == null) {
                tabLayout.setVisibility(View.GONE);
                return;
            } else {
                tabLayout.setVisibility(View.VISIBLE);
            }
        }

        if (partyMemberListFragment != null && group != null) {
            partyMemberListFragment.setPartyId(group.getId());
        }

        if (chatListFragment != null && group != null) {
            chatListFragment.setSeenGroupId(group.getId());
        }

        if (this.activity != null) {
            this.activity.supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.group != null && this.user != null) {
            if (this.group.getLeaderID().equals(this.user.getId())) {
                inflater.inflate(R.menu.menu_party_admin, menu);
            } else {
                inflater.inflate(R.menu.menu_party, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                new AlertDialog.Builder(viewPager.getContext())
                        .setTitle(viewPager.getContext().getString(R.string.leave_party))
                        .setMessage(viewPager.getContext().getString(R.string.leave_party_confirmation))
                        .setPositiveButton(viewPager.getContext().getString(R.string.yes), (dialog, which) ->  {
                            if (this.group != null){
                                this.socialRepository.leaveGroup(this.group.getId())
                                        .subscribe(group -> getActivity().getSupportFragmentManager().beginTransaction().remove(PartyFragment.this).commit(), RxErrorHandler.handleEmptyError());
                            }
                        })
                        .setNegativeButton(viewPager.getContext().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayEditForm() {
        Bundle bundle = new Bundle();
        bundle.putString("groupID", this.group != null ? this.group.getId() : null);
        bundle.putString("name", this.group.getName());
        bundle.putString("description", this.group.getDescription());
        bundle.putString("leader", this.group.getLeaderID());

        Intent intent = new Intent(activity, GroupFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, GroupFormActivity.GROUP_FORM_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (GroupFormActivity.GROUP_FORM_ACTIVITY): {
                if (resultCode == Activity.RESULT_OK) {
                    boolean needsSaving = false;
                    Bundle bundle = data.getExtras();
                    if (this.group == null) {
                        break;
                    }
                    this.socialRepository.updateGroup(this.group, bundle.getString("name"), bundle.getString("description"), bundle.getString("leader"), bundle.getString("privacy"))
                            .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
                }
                break;
            }
            case (PartyInviteActivity.RESULT_SEND_INVITES): {
                if (resultCode == Activity.RESULT_OK) {
                    Map<String, Object> inviteData = new HashMap<>();
                    inviteData.put("inviter", this.user != null ? this.user.getProfile().getName() : null);
                    if (data.getBooleanExtra(PartyInviteActivity.IS_EMAIL_KEY, false)) {
                        String[] emails = data.getStringArrayExtra(PartyInviteActivity.EMAILS_KEY);
                        List<HashMap<String, String>> invites = new ArrayList<>();
                        for (String email : emails) {
                            HashMap<String, String> invite = new HashMap<>();
                            invite.put("name", "");
                            invite.put("email", email);
                            invites.add(invite);
                        }
                        inviteData.put("emails", invites);
                    } else {
                        String[] userIDs = data.getStringArrayExtra(PartyInviteActivity.USER_IDS_KEY);
                        List<String> invites = new ArrayList<>();
                        Collections.addAll(invites, userIDs);
                        inviteData.put("uuids", invites);
                    }
                    if (this.group != null) {
                        this.socialRepository.inviteToGroup(this.group.getId(), inviteData)
                                .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
                    }
                }
            }
        }
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        if (this.user == null) {
            return;
        }

        UserParty party = this.user.getParty();

        if (party == null) {
            return;
        }

        viewPagerAdapter = new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 0: {
                        if (user.hasParty()) {
                            PartyDetailFragment detailFragment = new PartyDetailFragment();
                            detailFragment.partyId = user.getParty().id;
                            fragment = detailFragment;
                        } else {
                            fragment = GroupInformationFragment.Companion.newInstance(null, user);
                        }
                        break;
                    }
                    case 1: {
                        if (chatListFragment == null) {
                            chatListFragment = new ChatListFragment();
                            if (user.hasParty()) {
                                chatListFragment.configure(user.getParty().id, user, false);
                            }
                        }
                        fragment = chatListFragment;
                        break;
                    }
                    case 2: {
                        if (partyMemberListFragment == null) {
                            partyMemberListFragment = new PartyMemberListFragment();
                            if (user.hasParty()) {
                                partyMemberListFragment.setPartyId(user.getParty().id);
                            }
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
                if (group == null) {
                    return 1;
                } else {
                    return 3;
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getContext().getString(R.string.party);
                    case 1:
                        return getContext().getString(R.string.chat);
                    case 2:
                        return getContext().getString(R.string.members);
                }
                return "";
            }
        };
        this.viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && group != null) {
                    if (chatListFragment != null) {
                        chatListFragment.setNavigatedToFragment(group.getId());

                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1 && group != null) {
                    if (chatListFragment != null) {
                        chatListFragment.setNavigatedToFragment(group.getId());
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }


    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.sidebar_party);
    }

}
