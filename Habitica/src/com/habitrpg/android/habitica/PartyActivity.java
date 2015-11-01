package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.fragments.ChatListFragment;
import com.habitrpg.android.habitica.ui.fragments.PartyInformationFragment;
import com.habitrpg.android.habitica.ui.fragments.PartyMemberListFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;

import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PartyActivity extends AvatarActivityBase implements AppBarLayout.OnOffsetChangedListener {

    @InjectView(R.id.appbar)
    AppBarLayout appBarLayout;

    private APIHelper mAPIHelper;

    private Group group;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_party;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar)
                .build();

        setViewPagerAdapter();

        this.hostConfig = PrefsActivity.fromContext(this);
        User = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).querySingle();

        mAPIHelper = new APIHelper(this, hostConfig);

        updateUserAvatars();

        final ContentCache contentCache = new ContentCache(mAPIHelper.apiService);


        // Get the full group data
        mAPIHelper.apiService.getGroup("party", new Callback<Group>() {
            @Override
            public void success(Group group, Response response) {
                PartyActivity.this.group = group;

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
                            partyInformationFragment.setQuestContent(content);
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    private HashMap<Integer, Fragment> fragmentDictionary = new HashMap<>();

    private PartyMemberListFragment partyMemberListFragment;
    private PartyInformationFragment partyInformationFragment;


    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

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
                        fragment = new ChatListFragment(PartyActivity.this, "party", mAPIHelper, User, false);
                        break;
                    }
                    case 2: {
                        fragment = partyMemberListFragment = new PartyMemberListFragment(PartyActivity.this, group);
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


        detail_tabs.setupWithViewPager(viewPager);
    }


    private void updateUserAvatars() {
        avatarInHeader.updateData(User);
    }

    //region Events

    // until there is a party event
    public void onEvent(CreateTagCommand event) {

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        Fragment fragment = fragmentDictionary.get(viewPager.getCurrentItem());

        if (!(fragment instanceof ChatListFragment))
            return;

        ChatListFragment chatFragment = (ChatListFragment) fragment;

        // Disable Refresh if Header is collapsed

        if (i == 0) {
            chatFragment.setRefreshEnabled(true);
        } else {
            chatFragment.setRefreshEnabled(false);
        }
    }

    //endregion Events
}
