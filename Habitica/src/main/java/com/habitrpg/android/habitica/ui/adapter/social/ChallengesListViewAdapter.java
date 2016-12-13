package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.JoinChallengeCommand;
import com.habitrpg.android.habitica.events.commands.LeaveChallengeCommand;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeTasksCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;


public class ChallengesListViewAdapter extends RecyclerView.Adapter<ChallengesListViewAdapter.ChallengeViewHolder> {


    private List<Challenge> challenges = new ArrayList<>();

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges;
        this.notifyDataSetChanged();
    }

    @Override
    public ChallengeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.challenge_item, parent, false);

        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChallengeViewHolder holder, int position) {
        holder.bind(challenges.get(position));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public void addChallange(Challenge challenge) {
        challenges.add(challenge);
        notifyDataSetChanged();
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

        @BindView(R.id.challenge_button_join)
        Button joinButton;

        @BindView(R.id.challenge_button_leave)
        Button leaveButton;

        @BindView(R.id.user_background_layout)
        LinearLayout leaderLayout;

        @BindView(R.id.user_label)
        TextView leaderLabel;

        private Challenge challenge;

        public ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            joinButton.setOnClickListener(this);
            joinButton.setVisibility(View.INVISIBLE);

            leaveButton.setOnClickListener(this);
            leaveButton.setVisibility(View.INVISIBLE);

            leaderLayout.setOnClickListener(this);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(challenge.name);
            challengeDescription.setText(challenge.description);

            DataBindingUtils.setRoundedBackgroundInt(leaderLayout, android.R.color.darker_gray);
            DataBindingUtils.setForegroundTintColor(leaderLabel, android.R.color.white);
            leaderLabel.setText(String.format(getContext().getString(R.string.byLeader), challenge.leaderName));

            memberCountTextView.setText(challenge.memberCount + "");

            if (challenge.prize == 0) {
                gem_prize_layout.setVisibility(View.GONE);
            } else {
                gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(challenge.prize + "");
            }

            if (leaveButton != null && joinButton != null) {
                boolean userIdExists = challenge.user_id != null && !challenge.user_id.isEmpty();

                leaveButton.setVisibility(userIdExists ? View.VISIBLE : View.INVISIBLE);
                joinButton.setVisibility(userIdExists ? View.INVISIBLE : View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            if (view == leaderLayout) {
                EventBus.getDefault().post(new OpenFullProfileCommand(challenge.leaderId));
            } else if (view == joinButton) {
                EventBus.getDefault().post(new JoinChallengeCommand(challenge.id));
            } else if (view == leaveButton) {
                Context context = view.getContext();
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.challenge_leave_title))
                        .setMessage(String.format(context.getString(R.string.challenge_leave_text), challenge.name))
                        .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                            EventBus.getDefault().post(new LeaveChallengeCommand(challenge.id));
                        }).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {
                    dialog.dismiss();
                }).show();
            } else if (challenge != null) {
                // Card tapped
                EventBus.getDefault().post(new ShowChallengeTasksCommand(challenge.id));
            }
        }
    }
}