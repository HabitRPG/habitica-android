package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailActivityCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailDialogCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

public class ChallengesOverviewFragment extends BaseMainFragment {

    @Inject
    ChallengeRepository challengeRepository;

    public ViewPager viewPager;
    public FragmentStatePagerAdapter statePagerAdapter;
    private ChallengeListFragment userChallengesFragment;
    private ChallengeListFragment availableChallengesFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

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
    public void onDestroy() {
        challengeRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        statePagerAdapter = new FragmentStatePagerAdapter(fragmentManager) {

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
        };
        viewPager.setAdapter(statePagerAdapter);

        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    @Subscribe
    public void onEvent(ShowChallengeDetailDialogCommand cmd) {
        challengeRepository.getChallenge(cmd.challengeId).firstElement().subscribe(challenge -> ChallengeDetailDialogHolder.Companion.showDialog(getActivity(), challengeRepository, challenge,
        challenge1 -> {
            // challenge left
        }), RxErrorHandler.handleEmptyError());
    }

    @Subscribe
    public void onEvent(ShowChallengeDetailActivityCommand cmd) {
        Bundle bundle = new Bundle();
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, cmd.challengeId);

        Intent intent = new Intent(getActivity(), ChallengeDetailActivity.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.challenges);
    }
}
