package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailActivityCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailDialogCommand;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Stack;

import rx.subjects.PublishSubject;

public class ChallengesOverviewFragment extends BaseMainFragment {

    public ViewPager viewPager;
    public FragmentStatePagerAdapter statePagerAdapter;
    int currentPage;
    private Stack<Integer> pageHistory;
    private boolean saveToHistory;
    private PublishSubject<ArrayList<Challenge>> getUserChallengesObservable;
    private ChallengeListFragment userChallengesFragment;
    private ChallengeListFragment availableChallengesFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        setViewPagerAdapter();

        getUserChallengesObservable = PublishSubject.create();

        subscribeGetChallenges();

        userChallengesFragment = new ChallengeListFragment();
        userChallengesFragment.setUser(this.user);
        userChallengesFragment.setRefreshingCallback(this::subscribeGetChallenges);
        userChallengesFragment.setObservable(getUserChallengesObservable);
        userChallengesFragment.setViewUserChallengesOnly(true);

        availableChallengesFragment = new ChallengeListFragment();
        availableChallengesFragment.setUser(this.user);
        availableChallengesFragment.setRefreshingCallback(this::subscribeGetChallenges);
        availableChallengesFragment.setObservable(getUserChallengesObservable);
        availableChallengesFragment.setViewUserChallengesOnly(false);

        pageHistory = new Stack<>();

        return v;
    }

    private void subscribeGetChallenges() {
        this.apiHelper.apiService.getUserChallenges()
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(challenges -> {
                    getUserChallengesObservable.onNext(challenges);
                }, e -> {
                    getUserChallengesObservable.onError(e);
                });
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int newPageId) {
                if (saveToHistory)
                    pageHistory.push(currentPage);

                currentPage = newPageId;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        saveToHistory = true;

        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    @Subscribe
    public void onEvent(ShowChallengeDetailDialogCommand cmd) {
        Challenge challenge = new Select().from(Challenge.class).where(Condition.column("id").is(cmd.challengeId)).querySingle();

        ChallegeDetailDialogHolder.showDialog(HabiticaApplication.currentActivity, apiHelper, user, challenge, challenge1 -> {
            // challenge joined
            userChallengesFragment.addItem(challenge1);
            availableChallengesFragment.updateItem(challenge1);
        }, challenge1 -> {
            // challenge left
            userChallengesFragment.onRefresh();
            availableChallengesFragment.onRefresh();
        });
    }

    @Subscribe
    public void onEvent(ShowChallengeDetailActivityCommand cmd) {
        Bundle bundle = new Bundle();
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, cmd.challengeId);

        Intent intent = new Intent(HabiticaApplication.currentActivity, ChallengeDetailActivity.class);
        intent.putExtras(bundle);
        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        HabiticaApplication.currentActivity.startActivity(intent);
    }

    public boolean onHandleBackPressed() {
        if (!pageHistory.empty()) {
            saveToHistory = false;
            viewPager.setCurrentItem(pageHistory.pop());
            saveToHistory = true;

            return true;
        }

        return false;
    }

    @Override
    public String customTitle() {
        return getString(R.string.challenges);
    }
}
