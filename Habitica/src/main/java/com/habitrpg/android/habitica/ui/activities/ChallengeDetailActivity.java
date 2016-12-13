package com.habitrpg.android.habitica.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeTasksFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeDetailActivity extends BaseActivity {

    public static String CHALLENGE_ID = "CHALLENGE_ID";

    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    public APIHelper apiHelper;

    private ChallengeViewHolder challengeViewHolder;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_challenge_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar(toolbar);

        getSupportActionBar().setTitle(R.string.challenge_details);

        Bundle extras = getIntent().getExtras();

        String challengeId = extras.getString(CHALLENGE_ID);

        ChallengeTasksFragment fragment = new ChallengeTasksFragment();
        fragment.setTabLayout(detail_tabs);
        fragment.setUser(HabiticaApplication.User);
        fragment.setChallengeId(challengeId);

        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        }

        apiHelper.apiService.getChallenge(challengeId)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(challenge -> challengeViewHolder.bind(challenge), throwable -> {
                });

        challengeViewHolder = new ChallengeViewHolder(findViewById(R.id.challenge_header));
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.challenge_name)
        TextView challengeName;

        @BindView(R.id.challenge_description)
        TextView challengeDescription;


        @BindView(R.id.memberCountTextView)
        TextView memberCountTextView;

        @BindView(R.id.gem_prize_layout)
        LinearLayout gem_prize_layout;

        @BindView(R.id.gemPrizeTextView)
        TextView gemPrizeTextView;

        private Challenge challenge;

        public ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(challenge.name);
            challengeDescription.setText(challenge.description);

            memberCountTextView.setText(challenge.memberCount + "");

            if (challenge.prize == 0) {
                gem_prize_layout.setVisibility(View.GONE);
            } else {
                gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(challenge.prize + "");
            }
        }

        @Override
        public void onClick(View view) {

        }
    }
}
