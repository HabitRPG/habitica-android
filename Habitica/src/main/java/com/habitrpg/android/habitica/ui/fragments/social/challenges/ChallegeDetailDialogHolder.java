package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

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
    EmojiTextView challengeName;

    @BindView(R.id.challenge_description)
    EmojiTextView challengeDescription;

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
    private Action1<Challenge> challengeJoinedAction;
    private Action1<Challenge> challengeLeftAction;
    private Activity context;


    protected ChallegeDetailDialogHolder(View view, Activity context) {
        this.context = context;
        ButterKnife.bind(this, view);
    }

    public void bind(AlertDialog dialog, APIHelper apiHelper, HabitRPGUser user, Challenge challenge,
                     Action1<Challenge> challengeJoinedAction, Action1<Challenge> challengeLeftAction) {
        this.dialog = dialog;
        this.apiHelper = apiHelper;
        this.user = user;
        this.challenge = challenge;
        this.challengeJoinedAction = challengeJoinedAction;
        this.challengeLeftAction = challengeLeftAction;

        changeViewsByChallenge(challenge);
    }

    public void changeViewsByChallenge(Challenge challenge) {
        setJoined(challenge.user_id != null && !challenge.user_id.isEmpty());

        challengeName.setText(EmojiParser.parseEmojis(challenge.name));
        challengeDescription.setText(MarkdownParser.parseMarkdown(challenge.description));
        challengeLeader.setText(challenge.leaderName);

        gem_amount.setText(challenge.prize + "");
        member_count.setText(challenge.memberCount + "");
    }

    public void setJoined(boolean joined) {
        joinedHeader.setVisibility(joined ? View.VISIBLE : View.GONE);
        leaveButton.setVisibility(joined ? View.VISIBLE : View.GONE);

        notJoinedHeader.setVisibility(joined ? View.GONE : View.VISIBLE);
        joinButton.setVisibility(joined ? View.GONE : View.VISIBLE);
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
        context.startActivity(intent);
        this.dialog.dismiss();
    }

    @OnClick(R.id.challenge_join_btn)
    public void joinChallenge() {
        this.apiHelper.apiService.joinChallenge(challenge.id)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(challenge -> {
                    challenge.user_id = this.user.getId();
                    challenge.async().save();

                    if (challengeJoinedAction != null) {
                        challengeJoinedAction.call(challenge);
                    }

                    changeViewsByChallenge(challenge);
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


                                this.dialog.dismiss();
                            }, throwable -> {
                            });
                }).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }


    public static void showDialog(Activity activity, APIHelper apiHelper, HabitRPGUser user, Challenge challenge,
                                  Action1<Challenge> challengeJoinedAction, Action1<Challenge> challengeLeftAction) {
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_challenge_detail, null);

        ChallegeDetailDialogHolder challegeDetailDialogHolder = new ChallegeDetailDialogHolder(dialogLayout, activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(dialogLayout);

        challegeDetailDialogHolder.bind(builder.show(), apiHelper, user, challenge, challengeJoinedAction, challengeLeftAction);
    }
}