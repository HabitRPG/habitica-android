package com.habitrpg.android.habitica.ui.adapter.social;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.ShowChallengeTasksCommand;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

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

    public class ChallengeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.challenge_name)
        TextView challengeName;

        @BindView(R.id.leader_label)
        TextView leaderName;

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

            itemView.setOnClickListener(this);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(challenge.name);

            leaderName.setText(getContext().getString(R.string.by) + " " + getLeaderName(challenge.leader));

            memberCountTextView.setText(challenge.memberCount + "");

            if (challenge.prize == 0) {
                gem_prize_layout.setVisibility(View.GONE);
            } else {
                gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(challenge.prize + "");
            }
        }

        private String getLeaderName(HabitRPGUser user) {
            try {
                return user.getProfile().getName();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        public void onClick(View view) {
            if (challenge != null) {
                EventBus.getDefault().post(new ShowChallengeTasksCommand(challenge.id));
            }
        }
    }
}