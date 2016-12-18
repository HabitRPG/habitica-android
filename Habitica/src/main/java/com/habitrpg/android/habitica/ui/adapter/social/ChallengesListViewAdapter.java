package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        @Nullable
        @BindView(R.id.challenge_name)
        TextView challengeName;

        @BindView(R.id.challenge_description)
        TextView challengeDescription;

        @BindView(R.id.challenge_task_summary)
        TextView challengeTaskSummary;

        @BindView(R.id.officialHabiticaChallengeLayout)
        LinearLayout officialChallengeLayout;

        @Nullable
        @BindView(R.id.memberCountTextView)
        TextView memberCountTextView;

        @BindView(R.id.gemPrizeTextView)
        TextView gemPrizeTextView;

        private Challenge challenge;

        public ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(challenge.name);

            challengeDescription.setText(challenge.groupName);

            List<String> taskSummary = new ArrayList<>();
            HashMap<String, String[]> tasksOrder = challenge.getTasksOrder();
            for (Map.Entry<String, String[]> stringEntry : tasksOrder.entrySet()) {
                if(stringEntry.getValue().length != 0)
                {
                    taskSummary.add(stringEntry.getValue().length + " " + getLabelByTypeAndCount(stringEntry.getKey(), stringEntry.getValue().length));
                }
            }

            officialChallengeLayout.setVisibility(challenge.official ? View.VISIBLE : View.GONE);

            challengeTaskSummary.setText(TextUtils.join(" | ", taskSummary));

            //DataBindingUtils.setRoundedBackgroundInt(leaderLayout, android.R.color.darker_gray);
            //DataBindingUtils.setForegroundTintColor(leaderLabel, android.R.color.white);
            //leaderLabel.setText(String.format(getContext().getString(R.string.byLeader), challenge.leaderName));

            //memberCountTextView.setText(challenge.memberCount + "");

            if (challenge.prize == 0) {
                //gem_prize_layout.setVisibility(View.GONE);
            } else {
                //gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(challenge.prize + "");
            }

            /*if (leaveButton != null && joinButton != null) {
                boolean userIdExists = challenge.user_id != null && !challenge.user_id.isEmpty();

                leaveButton.setVisibility(userIdExists ? View.VISIBLE : View.INVISIBLE);
                joinButton.setVisibility(userIdExists ? View.INVISIBLE : View.VISIBLE);
            }*/
        }

        private String getLabelByTypeAndCount(String type, int count){
            if(type == Challenge.TASK_ORDER_DAILYS){
                 return getContext().getString(count == 1 ? R.string.daily : R.string.dailies);
            } else if(type == Challenge.TASK_ORDER_HABITS){
                return getContext().getString(count == 1 ? R.string.habit : R.string.habits);
            } else if(type == Challenge.TASK_ORDER_REWARDS){
                return getContext().getString(count == 1 ? R.string.reward : R.string.rewards);
            } else {
                return getContext().getString(count == 1 ? R.string.todo : R.string.todos);
            }
        }

        @Override
        public void onClick(View view) {
            /*if (view == leaderLayout) {
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
            } else*/if (challenge != null) {
                // Card tapped
                EventBus.getDefault().post(new ShowChallengeTasksCommand(challenge.id));
            }
        }
    }
}