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
import com.habitrpg.android.habitica.events.commands.ShowChallengeTasksCommand;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;

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
        viewPager.setCurrentItem(1);

        setViewPagerAdapter();

        challengeListFragment = new ChallengeListFragment();
        challengeListFragment.setUser(this.user);
        challengeListFragment.setViewUserChallengesOnly(true);

        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private ChallengeListFragment challengeListFragment;

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                Fragment fragment = new Fragment();

                switch (position) {
                    case 0:
                        return challengeListFragment;
                    case 1:
                        return fragment;
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
                        return "My Challenges";
                    case 1:
                        return "Public";
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
}
