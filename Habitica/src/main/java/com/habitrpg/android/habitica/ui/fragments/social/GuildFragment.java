package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

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

import rx.functions.Action1;

public class GuildFragment extends BaseMainFragment implements Action1<Group> {

    public boolean isMember;
    public ViewPager viewPager;
    private Group guild;
    private GroupInformationFragment guildInformationFragment;
    private ChatListFragment chatListFragment;

    public void setGuild(Group guild) {
        this.guild = guild;
        if (this.guildInformationFragment != null) {
            this.guildInformationFragment.setGroup(guild);
        }
        if (this.guild.chat == null) {
            if (this.apiHelper != null) {
                apiHelper.apiService.getGroup(this.guild.id).compose(apiHelper.configureApiCallObserver())
                        .subscribe(this, throwable -> {
                        });
            }
        }
    }

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.apiHelper != null && this.guild != null) {
            apiHelper.apiService.getGroup(this.guild.id).compose(apiHelper.configureApiCallObserver())
                    .subscribe(this, throwable -> {
                    });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.isMember) {
            if (this.user != null && this.user.getId().equals(this.guild.leaderID)) {
                this.activity.getMenuInflater().inflate(R.menu.guild_admin, menu);
            } else {
                this.activity.getMenuInflater().inflate(R.menu.guild_member, menu);
            }
        } else {
            this.activity.getMenuInflater().inflate(R.menu.guild_nonmember, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_guild_join:
                this.apiHelper.apiService.joinGroup(this.guild.id).compose(apiHelper.configureApiCallObserver())
                        .subscribe(this, throwable -> {
                        });
                this.isMember = true;
                return true;
            case R.id.menu_guild_leave:
                this.apiHelper.apiService.leaveGroup(this.guild.id).compose(apiHelper.configureApiCallObserver())
                        .subscribe(aVoid -> {
                            this.activity.supportInvalidateOptionsMenu();
                        }, throwable -> {
                        });
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
                        fragment = guildInformationFragment = GroupInformationFragment.newInstance(GuildFragment.this.guild, user);
                        break;
                    }
                    case 1: {
                        chatListFragment = new ChatListFragment();
                        chatListFragment.configure(GuildFragment.this.guild.id, user, false);
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
                        return activity.getString(R.string.guild);
                    case 1:
                        return activity.getString(R.string.chat);
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
                if (position == 1 && GuildFragment.this.guild != null) {
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
                    boolean needsSaving = false;
                    Bundle bundle = data.getExtras();
                    if (this.guild.name != null && !this.guild.name.equals(bundle.getString("name"))) {
                        this.guild.name = bundle.getString("name");
                        needsSaving = true;
                    }
                    if (this.guild.description != null && !this.guild.description.equals(bundle.getString("description"))) {
                        this.guild.description = bundle.getString("description");
                        needsSaving = true;
                    }
                    if (this.guild.leaderID != null && !this.guild.leaderID.equals(bundle.getString("leader"))) {
                        this.guild.leaderID = bundle.getString("leader");
                        needsSaving = true;
                    }
                    if (this.guild.privacy != null && !this.guild.privacy.equals(bundle.getString("privacy"))) {
                        this.guild.privacy = bundle.getString("privacy");
                        needsSaving = true;
                    }
                    if (needsSaving) {
                        this.apiHelper.apiService.updateGroup(this.guild.id, this.guild)
                                .compose(apiHelper.configureApiCallObserver())
                                .subscribe(aVoid -> {
                                }, throwable -> {
                                });
                        this.guildInformationFragment.setGroup(guild);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void call(Group group) {
        if (group != null) {
            if (this.guildInformationFragment != null) {
                this.guildInformationFragment.setGroup(group);
            }

            if (this.chatListFragment != null) {
                this.chatListFragment.seenGroupId = group.id;
            }

            this.guild = group;
        }
        this.activity.supportInvalidateOptionsMenu();
    }

	@Override
	public String customTitle() {
		return getString(R.string.guild);
	}
}
