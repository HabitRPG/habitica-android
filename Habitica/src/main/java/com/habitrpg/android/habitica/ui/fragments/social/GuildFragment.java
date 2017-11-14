package com.habitrpg.android.habitica.ui.fragments.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;

import javax.inject.Inject;

import rx.functions.Action1;

public class GuildFragment extends BaseMainFragment {

    @Inject
    SocialRepository socialRepository;

    public boolean isMember;
    public ViewPager viewPager;
    private Group guild;
    private GroupInformationFragment guildInformationFragment;
    private ChatListFragment chatListFragment;
    private String guildId;

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

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

        setViewPagerAdapter();

        if (guildId != null && this.socialRepository != null) {
            compositeSubscription.add(socialRepository.getGroup(this.guildId).subscribe(this::setGroup, RxErrorHandler.handleEmptyError()));
            socialRepository.retrieveGroup(this.guildId).subscribe(group -> {}, RxErrorHandler.handleEmptyError());
        }

        return v;
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.socialRepository != null && this.guild != null) {
            socialRepository.retrieveGroup(this.guild.id).subscribe(this::setGroup, RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.activity != null && this.guild != null) {
            if (this.isMember) {
                if (this.user != null && this.user.getId().equals(this.guild.leaderID)) {
                    this.activity.getMenuInflater().inflate(R.menu.guild_admin, menu);
                } else {
                    this.activity.getMenuInflater().inflate(R.menu.guild_member, menu);
                }
            } else {
                this.activity.getMenuInflater().inflate(R.menu.guild_nonmember, menu);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_guild_join:
                this.socialRepository.joinGroup(this.guild.id).subscribe(this::setGroup, RxErrorHandler.handleEmptyError());
                this.isMember = true;
                return true;
            case R.id.menu_guild_leave:
                this.socialRepository.leaveGroup(this.guild.id)
                        .subscribe(aVoid -> {
                            if (this.activity != null) {
                                this.activity.supportInvalidateOptionsMenu();
                            }
                        }, RxErrorHandler.handleEmptyError());
                this.isMember = false;
                return true;
            case R.id.menu_guild_edit:
                this.displayEditForm();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 0: {
                        fragment = guildInformationFragment = GroupInformationFragment.Companion.newInstance(GuildFragment.this.guild, user);
                        break;
                    }
                    case 1: {
                        chatListFragment = new ChatListFragment();
                        chatListFragment.configure(GuildFragment.this.guildId, user, false);
                        fragment = chatListFragment;
                        break;
                    }
                    default:
                        fragment = new Fragment();
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getContext().getString(R.string.guild);
                    case 1:
                        return getContext().getString(R.string.chat);
                }
                return "";
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && GuildFragment.this.guild != null) {
                    chatListFragment.setNavigatedToFragment(GuildFragment.this.guild.id);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1 && GuildFragment.this.guild != null && chatListFragment != null) {
                    chatListFragment.setNavigatedToFragment(GuildFragment.this.guild.id);
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

    private void displayEditForm() {
        Bundle bundle = new Bundle();
        bundle.putString("groupID", this.guild.id);
        bundle.putString("name", this.guild.name);
        bundle.putString("description", this.guild.description);
        bundle.putString("privacy", this.guild.privacy);
        bundle.putString("leader", this.guild.leaderID);

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
                    Bundle bundle = data.getExtras();
                    this.socialRepository.updateGroup(this.guild,
                            bundle.getString("name"),
                            bundle.getString("description"),
                            bundle.getString("leader"),
                            bundle.getString("privacy"))
                            .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
                }
                break;
            }
        }
    }

    public void setGroup(Group group) {
        if (group != null) {
            if (this.guildInformationFragment != null) {
                this.guildInformationFragment.setGroup(group);
            }

            if (this.chatListFragment != null) {
                this.chatListFragment.seenGroupId = group.id;
            }

            this.guild = group;
        }
        if (this.activity != null) {
            this.activity.supportInvalidateOptionsMenu();
        }
    }

    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.guild);
    }
}
