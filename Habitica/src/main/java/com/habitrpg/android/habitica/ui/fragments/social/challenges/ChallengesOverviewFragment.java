package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.JoinChallengeCommand;
import com.habitrpg.android.habitica.events.commands.LeaveChallengeCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeTasksCommand;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;

import org.greenrobot.eventbus.Subscribe;

public class ChallengesOverviewFragment extends BaseMainFragment {

    public ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        setViewPagerAdapter();

        userChallengesFragment = new ChallengeListFragment();
        userChallengesFragment.setUser(this.user);
        userChallengesFragment.setViewUserChallengesOnly(true);

        availableChallengesFragment = new ChallengeListFragment();
        availableChallengesFragment.setUser(this.user);
        availableChallengesFragment.setViewUserChallengesOnly(false);

        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private ChallengeListFragment userChallengesFragment;
    private ChallengeListFragment availableChallengesFragment;

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                Fragment fragment = new Fragment();

                switch (position) {
                    case 0:
                        return userChallengesFragment;
                    case 1:
                        return availableChallengesFragment;
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
                        return getString(R.string.my_challenges);
                    case 1:
                        return getString(R.string.public_challenges);
                }
                return "";
            }
        });

        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    @Subscribe
    public void onEvent(ShowChallengeTasksCommand cmd){

        Bundle bundle = new Bundle();
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, cmd.challengeId);

        Intent intent = new Intent(activity, ChallengeDetailActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(JoinChallengeCommand cmd){
        this.apiHelper.apiService.joinChallenge(cmd.challengeId)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(challenge -> {
                    challenge.user_id = this.user.getId();

                    userChallengesFragment.addItem(challenge);
                }, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(LeaveChallengeCommand cmd){
        this.apiHelper.apiService.leaveChallenge(cmd.challengeId)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(aVoid -> {

                    Challenge challenge = new Select().from(Challenge.class).byIds(cmd.challengeId).querySingle();
                    challenge.user_id = null;
                    challenge.save();

                    this.user.resetChallengeList();

                    userChallengesFragment.onRefresh();
                    availableChallengesFragment.onRefresh();
                }, throwable -> {
                });
    }

}
