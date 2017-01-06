package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailDialogCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailActivityCommand;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

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
    private boolean viewUserChallengesOnly;

    public ChallengesListViewAdapter(boolean viewUserChallengesOnly) {

        this.viewUserChallengesOnly = viewUserChallengesOnly;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges;
        this.notifyDataSetChanged();
    }

    @Override
    public ChallengeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.challenge_item, parent, false);

        return new ChallengeViewHolder(view, viewUserChallengesOnly);
    }

    @Override
    public void onBindViewHolder(ChallengeViewHolder holder, int position) {
        holder.bind(challenges.get(position));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public void addChallenge(Challenge challenge) {
        challenges.add(challenge);
        notifyDataSetChanged();
    }

    public void replaceChallenge(Challenge challenge) {
        int index = challenges.indexOf(challenge);

        if (index == -1) {
            for (int i = 0; i < challenges.size(); i++) {
                if (challenges.get(i).id.equals(challenge.id)) {
                    index = i;

                    break;
                }
            }
        }

        if (index != -1) {
            challenges.set(index, challenge);
            notifyItemChanged(index);
        }
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.challenge_name)
        EmojiTextView challengeName;

        @BindView(R.id.challenge_group_name)
        TextView challengeDescription;

        @BindView(R.id.challenge_task_summary)
        TextView challengeTaskSummary;

        @BindView(R.id.officialHabiticaChallengeLayout)
        LinearLayout officialChallengeLayout;

        @BindView(R.id.challenge_is_participating)
        TextView challengeParticipatingTextView;

        @Nullable
        @BindView(R.id.memberCountTextView)
        TextView memberCountTextView;

        @BindView(R.id.arrowImage)
        LinearLayout arrowImage;

        @BindView(R.id.gemPrizeTextView)
        TextView gemPrizeTextView;

        private Challenge challenge;
        private boolean viewUserChallengesOnly;

        public ChallengeViewHolder(View itemView, boolean viewUserChallengesOnly) {
            super(itemView);
            this.viewUserChallengesOnly = viewUserChallengesOnly;

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            if (!viewUserChallengesOnly) {
                challengeName.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_200));
            }
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(EmojiParser.parseEmojis(challenge.name.trim()));
            challengeDescription.setText(challenge.groupName);

            officialChallengeLayout.setVisibility(challenge.official ? View.VISIBLE : View.GONE);
            boolean userIdExists = challenge.user_id != null && !challenge.user_id.isEmpty();

            if (viewUserChallengesOnly) {
                List<String> taskSummary = new ArrayList<>();

                HashMap<String, String[]> tasksOrder = challenge.getTasksOrder();
                for (Map.Entry<String, String[]> stringEntry : tasksOrder.entrySet()) {
                    if (stringEntry.getValue().length != 0) {
                        taskSummary.add(stringEntry.getValue().length + " " + getLabelByTypeAndCount(getContext(), stringEntry.getKey(), stringEntry.getValue().length));
                    }
                }

                challengeTaskSummary.setText(TextUtils.join(" | ", taskSummary));
                challengeParticipatingTextView.setVisibility(View.GONE);
                arrowImage.setVisibility(View.VISIBLE);
            } else {
                challengeParticipatingTextView.setVisibility(userIdExists ? View.VISIBLE : View.GONE);

                challengeTaskSummary.setText(String.format(getContext().getString(R.string.byLeader), challenge.leaderName) + " | " +
                        challenge.memberCount + " " + getContext().getString(R.string.quest_participants));
                arrowImage.setVisibility(View.GONE);
            }

            if (challenge.prize == 0) {
                //gem_prize_layout.setVisibility(View.GONE);
            } else {
                //gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(challenge.prize + "");
            }
        }

        public static String getLabelByTypeAndCount(Context context, String type, int count) {
            if (type == Challenge.TASK_ORDER_DAILYS) {
                return context.getString(count == 1 ? R.string.daily : R.string.dailies);
            } else if (type == Challenge.TASK_ORDER_HABITS) {
                return context.getString(count == 1 ? R.string.habit : R.string.habits);
            } else if (type == Challenge.TASK_ORDER_REWARDS) {
                return context.getString(count == 1 ? R.string.reward : R.string.rewards);
            } else {
                return context.getString(count == 1 ? R.string.todo : R.string.todos);
            }
        }

        @Override
        public void onClick(View view) {
            if (challenge != null) {
                if (viewUserChallengesOnly) {
                    EventBus.getDefault().post(new ShowChallengeDetailActivityCommand(challenge.id));
                } else {
                    EventBus.getDefault().post(new ShowChallengeDetailDialogCommand(challenge.id));
                }
            }
        }
    }
}