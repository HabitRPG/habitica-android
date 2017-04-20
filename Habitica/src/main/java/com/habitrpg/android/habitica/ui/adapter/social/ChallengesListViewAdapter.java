package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.underscore.$;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailActivityCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailDialogCommand;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeFilterOptions;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengesListViewAdapter extends RecyclerView.Adapter<ChallengesListViewAdapter.ChallengeViewHolder> {


    private List<Challenge> challenges = new ArrayList<>();
    private List<Challenge> challengesSource = new ArrayList<>();

    private boolean viewUserChallengesOnly;
    @Nullable
    private User user;

    public ChallengesListViewAdapter(boolean viewUserChallengesOnly, @Nullable User user) {
        this.viewUserChallengesOnly = viewUserChallengesOnly;
        this.user = user;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challengesSource = challenges;
        this.challenges = new ArrayList<>(challengesSource);
        this.notifyDataSetChanged();
    }

    public void setFilterByGroups(ChallengeFilterOptions filterOptions){
        this.challenges = $.filter(challengesSource, arg -> {
            boolean showChallenge = $.find(filterOptions.showByGroups, g -> g.id.contains(arg.groupId)).isPresent();

            boolean showByOwnership = true;
            if(filterOptions.showOwned == filterOptions.notOwned && this.user != null){
                if (filterOptions.showOwned) {
                    showByOwnership = arg.leaderId.equals(this.user.getId());
                } else {
                    showByOwnership = !arg.leaderId.equals(this.user.getId());
                }
            }

            return showChallenge && showByOwnership;
        });
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

        @BindView(R.id.leaderParticipantLayout)
        LinearLayout leaderParticipantLayout;

        @BindView(R.id.leaderName)
        TextView leaderName;

        @BindView(R.id.participantCount)
        TextView participantCount;

        @BindView(R.id.officialHabiticaChallengeLayout)
        LinearLayout officialChallengeLayout;

        @BindView(R.id.challenge_is_participating)
        View challengeParticipatingTextView;

        @Nullable
        @BindView(R.id.memberCountTextView)
        TextView memberCountTextView;

        @BindView(R.id.arrowImage)
        LinearLayout arrowImage;

        @BindView(R.id.gemPrizeTextView)
        TextView gemPrizeTextView;

        private Challenge challenge;
        private boolean viewUserChallengesOnly;

        ChallengeViewHolder(View itemView, boolean viewUserChallengesOnly) {
            super(itemView);
            this.viewUserChallengesOnly = viewUserChallengesOnly;

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            if (!viewUserChallengesOnly) {
                challengeName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.brand_200));
            }
        }

        public static String getLabelByTypeAndCount(Context context, String type, int count) {
            if (Challenge.TASK_ORDER_DAILYS.equals(type)) {
                return context.getString(count == 1 ? R.string.daily : R.string.dailies);
            } else if (Challenge.TASK_ORDER_HABITS.equals(type)) {
                return context.getString(count == 1 ? R.string.habit : R.string.habits);
            } else if (Challenge.TASK_ORDER_REWARDS.equals(type)) {
                return context.getString(count == 1 ? R.string.reward : R.string.rewards);
            } else {
                return context.getString(count == 1 ? R.string.todo : R.string.todos);
            }
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(EmojiParser.parseEmojis(challenge.name.trim()));
            challengeDescription.setText(challenge.groupName);

            officialChallengeLayout.setVisibility(challenge.official ? View.VISIBLE : View.GONE);
            boolean userIdExists = challenge.user_id != null && !challenge.user_id.isEmpty();

            if (viewUserChallengesOnly) {
                leaderParticipantLayout.setVisibility(View.GONE);
                challengeParticipatingTextView.setVisibility(View.GONE);
                arrowImage.setVisibility(View.VISIBLE);
            } else {
                challengeParticipatingTextView.setVisibility(userIdExists ? View.VISIBLE : View.GONE);

                leaderName.setText(itemView.getContext().getString(R.string.byLeader, challenge.leaderName));
                participantCount.setText(String.valueOf(challenge.memberCount));
                leaderParticipantLayout.setVisibility(View.VISIBLE);
                arrowImage.setVisibility(View.GONE);
            }

            gemPrizeTextView.setText(String.valueOf(challenge.prize));
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