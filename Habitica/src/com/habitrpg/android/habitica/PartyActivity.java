package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.fragments.ChatListFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;

import butterknife.InjectView;

public class PartyActivity extends AvatarActivityBase implements AppBarLayout.OnOffsetChangedListener {

    @InjectView(R.id.appbar)
    AppBarLayout appBarLayout;

    private APIHelper mAPIHelper;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_party;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar)
                .withSelectedItem(0)
                .build();

        setViewPagerAdapter();

        this.hostConfig = PrefsActivity.fromContext(this);
        User = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).querySingle();

        mAPIHelper = new APIHelper(this, hostConfig);


        updateUserAvatars();
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


    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 1:
                        fragment = new ChatListFragment(PartyActivity.this, "party", mAPIHelper, User, false);
                        break;
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
        avatarInHeader.UpdateData(User);
    }

    //region Events

    // until there is a party event
    public void onEvent(CreateTagCommand event) {

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        Fragment fragment = fragmentDictionary.get(viewPager.getCurrentItem());

        if(!(fragment instanceof ChatListFragment))
            return;

        ChatListFragment chatFragment = (ChatListFragment)fragment ;

        // Disable Refresh if Header is collapsed

        if (i == 0) {
            chatFragment.setRefreshEnabled(true);
        } else {
            chatFragment.setRefreshEnabled(false);
        }
    }

    //endregion Events
}
