package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeTasksCommand;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChallengesOverviewFragment extends BaseMainFragment {

    public ViewPager viewPager;
    public FragmentStatePagerAdapter statePagerAdapter;
    private Stack<Integer> pageHistory;
    private boolean saveToHistory;
    int currentPage;

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

        pageHistory = new Stack<Integer>();

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
                    pageHistory.push(Integer.valueOf(currentPage));

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
    public void onEvent(ShowChallengeTasksCommand cmd) {

        View dialogLayout = HabiticaApplication.currentActivity.getLayoutInflater().inflate(R.layout.dialog_challenge_detail, null);

        Challenge challenge = new Select().from(Challenge.class).where(Condition.column("id").is(cmd.challengeId)).querySingle();

        ChallegeDetailDialogHolder challegeDetailDialogHolder = new ChallegeDetailDialogHolder(dialogLayout, activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(HabiticaApplication.currentActivity)
                .setView(dialogLayout);

        challegeDetailDialogHolder.bind(builder.show(), apiHelper, user, challenge);
    }

    public boolean onHandleBackPressed() {
        if (!pageHistory.empty()) {
            saveToHistory = false;
            viewPager.setCurrentItem(pageHistory.pop().intValue());
            saveToHistory = true;

            return true;
        }

        return false;
    }

    public class ChallegeDetailDialogHolder {

        @BindView(R.id.challenge_not_joined_header)
        LinearLayout notJoinedHeader;

        @BindView(R.id.challenge_joined_header)
        LinearLayout joinedHeader;

        @BindView(R.id.challenge_join_btn)
        Button joinButton;

        @BindView(R.id.challenge_leave_btn)
        Button leaveButton;

        @BindView(R.id.challenge_name)
        TextView challengeName;

        @BindView(R.id.challenge_description)
        TextView challengeDescription;

        @BindView(R.id.challenge_leader)
        TextView challengeLeader;

        @BindView(R.id.gem_amount)
        TextView gem_amount;

        @BindView(R.id.challenge_member_count)
        TextView member_count;


        private AlertDialog dialog;
        private APIHelper apiHelper;
        private HabitRPGUser user;
        private Challenge challenge;
        private Context context;


        protected ChallegeDetailDialogHolder(View view, Context context) {
            this.context = context;
            ButterKnife.bind(this, view);
        }

        public void bind(AlertDialog dialog, APIHelper apiHelper, HabitRPGUser user, Challenge challenge) {
            this.dialog = dialog;
            this.apiHelper = apiHelper;
            this.user = user;
            this.challenge = challenge;

            if (challenge.user_id == null || challenge.user_id.isEmpty()) {
                notJoinedHeader.setVisibility(View.VISIBLE);
                joinButton.setVisibility(View.VISIBLE);
            } else {
                joinedHeader.setVisibility(View.VISIBLE);
                leaveButton.setVisibility(View.VISIBLE);
            }

            challengeName.setText(challenge.name);
            challengeDescription.setText(challenge.description);
            challengeLeader.setText(challenge.leaderName);

            gem_amount.setText(challenge.prize + "");
            member_count.setText(challenge.memberCount + "");
        }

        @OnClick(R.id.challenge_leader)
        public void openLeaderProfile() {
            EventBus.getDefault().post(new OpenFullProfileCommand(challenge.leaderId));
        }

        @OnClick(R.id.challenge_go_to_btn)
        public void openChallengeActivity() {
            Bundle bundle = new Bundle();
            bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, challenge.id);

            Intent intent = new Intent(HabiticaApplication.currentActivity, ChallengeDetailActivity.class);
            intent.putExtras(bundle);
            //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            this.dialog.dismiss();
        }

        @OnClick(R.id.challenge_join_btn)
        public void joinChallenge() {
            this.apiHelper.apiService.joinChallenge(challenge.id)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(challenge -> {
                        challenge.user_id = this.user.getId();
                        challenge.async().save();

                        userChallengesFragment.addItem(challenge);
                        this.dialog.dismiss();
                    }, throwable -> {
                    });
        }

        @OnClick(R.id.challenge_leave_btn)
        public void leaveChallenge() {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.challenge_leave_title))
                    .setMessage(String.format(context.getString(R.string.challenge_leave_text), challenge.name))
                    .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                        this.apiHelper.apiService.leaveChallenge(challenge.id)
                                .compose(apiHelper.configureApiCallObserver())
                                .subscribe(aVoid -> {
                                    challenge.user_id = null;
                                    challenge.async().save();

                                    this.user.resetChallengeList();

                                    userChallengesFragment.onRefresh();
                                    availableChallengesFragment.onRefresh();
                                    this.dialog.dismiss();
                                }, throwable -> {
                                });
                    }).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {
                dialog.dismiss();
            }).show();
        }
    }


    @Override
    public String customTitle() {
        return getString(R.string.challenges);
    }
}
