package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailActivityCommand;
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailDialogCommand;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeFilterOptions;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;

public class ChallengesListViewAdapter extends RealmRecyclerViewAdapter<Challenge, ChallengesListViewAdapter.ChallengeViewHolder> {

    private boolean viewUserChallengesOnly;
    private OrderedRealmCollection<Challenge> unfilteredData;
    private final String userId;

    public ChallengesListViewAdapter(@Nullable OrderedRealmCollection<Challenge> data, boolean autoUpdate, boolean viewUserChallengesOnly, String userId) {
        super(data, autoUpdate);
        this.viewUserChallengesOnly = viewUserChallengesOnly;
        this.userId = userId;
    }

    @Override
    public ChallengeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.challenge_item, parent, false);

        return new ChallengeViewHolder(view, viewUserChallengesOnly);
    }

    @Override
    public void onBindViewHolder(ChallengeViewHolder holder, int position) {
        if (getData() != null) {
            holder.bind(getData().get(position));
        }
    }

    public void updateUnfilteredData(@Nullable OrderedRealmCollection<Challenge> data) {
        super.updateData(data);
        unfilteredData = data;
    }

    public void filter(ChallengeFilterOptions filterOptions) {
        if (unfilteredData == null) {
            return;
        }

        RealmQuery<Challenge> query = unfilteredData.where();

        if (filterOptions.showByGroups != null && filterOptions.showByGroups.size() > 0) {
            String[] groupIds = new String[filterOptions.showByGroups.size()];
            int index = 0;
            for (Group group : filterOptions.showByGroups) {
                groupIds[index] = group.id;
                index += 1;
            }
            query = query.in("groupId", groupIds);
        }

        if (filterOptions.showOwned != filterOptions.notOwned) {
            if (filterOptions.showOwned) {
                query = query.equalTo("leaderId", userId);
            } else {
                query = query.notEqualTo("leaderId", userId);
            }
        }

        this.updateData(query.findAll());
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
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

        @BindView(R.id.gem_icon)
        ImageView gemIconView;

        private Challenge challenge;
        private boolean viewUserChallengesOnly;

        ChallengeViewHolder(View itemView, boolean viewUserChallengesOnly) {
            super(itemView);
            this.viewUserChallengesOnly = viewUserChallengesOnly;
            ButterKnife.bind(this, itemView);

            context = itemView.getContext();
            itemView.setOnClickListener(this);

            gemIconView.setImageBitmap(HabiticaIconsHelper.imageOfGem());

            if (!viewUserChallengesOnly) {
                challengeName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.brand_200));
            }
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            challengeName.setText(EmojiParser.parseEmojis(challenge.name.trim()));
            challengeDescription.setText(challenge.groupName);

            officialChallengeLayout.setVisibility(challenge.official ? View.VISIBLE : View.GONE);

            if (viewUserChallengesOnly) {
                leaderParticipantLayout.setVisibility(View.GONE);
                challengeParticipatingTextView.setVisibility(View.GONE);
                arrowImage.setVisibility(View.VISIBLE);
            } else {
                challengeParticipatingTextView.setVisibility(challenge.isParticipating ? View.VISIBLE : View.GONE);

                leaderName.setText(context.getString(R.string.byLeader, challenge.leaderName));
                participantCount.setText(String.valueOf(challenge.memberCount));
                leaderParticipantLayout.setVisibility(View.VISIBLE);
                arrowImage.setVisibility(View.GONE);
            }

            gemPrizeTextView.setText(String.valueOf(challenge.prize));
        }

        @Override
        public void onClick(View view) {
            if (challenge != null && challenge.isManaged()) {
                if (viewUserChallengesOnly) {
                    EventBus.getDefault().post(new ShowChallengeDetailActivityCommand(challenge.id));
                } else {
                    EventBus.getDefault().post(new ShowChallengeDetailDialogCommand(challenge.id));
                }
            }
        }
    }
}
