package com.habitrpg.android.habitica.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallegeDetailDialogHolder;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeTasksFragment;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChallengeDetailActivity extends BaseActivity {

    public static String CHALLENGE_ID = "CHALLENGE_ID";

    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    public APIHelper apiHelper;

    private ChallengeViewHolder challengeViewHolder;

    private Challenge challenge;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_challenge_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_challenge_details, menu);
        return true;
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

        challenge = new Select().from(Challenge.class).where(Condition.column("id").is(challengeId)).querySingle();

        challengeViewHolder = new ChallengeViewHolder(findViewById(R.id.challenge_header));
        challengeViewHolder.bind(challenge);
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    public class ChallengeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.challenge_name)
        EmojiTextView challengeName;

        @BindView(R.id.challenge_description)
        EmojiTextView challengeDescription;

        @BindView(R.id.challenge_member_count)
        TextView memberCountTextView;

        @BindView(R.id.gem_prize_layout)
        LinearLayout gem_prize_layout;

        @BindView(R.id.gem_amount)
        TextView gemPrizeTextView;

        private Challenge challenge;

        public ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(EmojiParser.parseEmojis(challenge.name));
            challengeDescription.setText(MarkdownParser.parseMarkdown(challenge.description));

            memberCountTextView.setText(challenge.memberCount + "");

            if (challenge.prize == 0) {
                gem_prize_layout.setVisibility(View.GONE);
            } else {
                gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(challenge.prize + "");
            }
        }

        @OnClick(R.id.btn_show_more)
        public void onShowMore() {

            ChallegeDetailDialogHolder.showDialog(ChallengeDetailActivity.this, ChallengeDetailActivity.this.apiHelper,
                    HabiticaApplication.User, challenge,
                    challenge1 -> {

                    },
                    challenge1 -> ChallengeDetailActivity.this.onBackPressed());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_leave:
                new AlertDialog.Builder(this)
                        .setTitle(this.getString(R.string.challenge_leave_title))
                        .setMessage(String.format(this.getString(R.string.challenge_leave_text), challenge.name))
                        .setPositiveButton(this.getString(R.string.yes), (dialog, which) -> {
                            this.apiHelper.apiService.leaveChallenge(challenge.id)
                                    .compose(apiHelper.configureApiCallObserver())
                                    .subscribe(aVoid -> {
                                        challenge.user_id = null;
                                        challenge.async().save();

                                        HabiticaApplication.User.resetChallengeList();
                                    }, throwable -> {
                                    });
                        })
                        .setNegativeButton(this.getString(R.string.no), (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
