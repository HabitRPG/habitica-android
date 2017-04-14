package com.habitrpg.android.habitica.ui.adapter.social;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;
import com.habitrpg.android.habitica.models.social.Group;

import org.greenrobot.eventbus.EventBus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublicGuildsRecyclerViewAdapter extends RecyclerView.Adapter<PublicGuildsRecyclerViewAdapter.GuildViewHolder> implements Filterable {

    public ApiClient apiClient;
    private List<Group> publicGuildList;
    private List<Group> fullPublicGuildList;
    private List<String> memberGuildIDs;

    public void setPublicGuildList(List<Group> publicGuildList) {
        this.publicGuildList = publicGuildList;
        this.fullPublicGuildList = new ArrayList<>(publicGuildList);
        this.notifyDataSetChanged();
    }

    public void setMemberGuildIDs(List<String> memberGuildIDs) {
        this.memberGuildIDs = memberGuildIDs;
    }

    @Override
    public GuildViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_public_guild, parent, false);
        GuildViewHolder guildViewHolder = new GuildViewHolder(view);
        guildViewHolder.itemView.setOnClickListener(v -> {
            Group guild = (Group) v.getTag();
            GuildFragment guildFragment = new GuildFragment();
            guildFragment.setGuild(guild);
            guildFragment.isMember = isInGroup(guild);
            DisplayFragmentEvent event = new DisplayFragmentEvent();
            event.fragment = guildFragment;
            EventBus.getDefault().post(event);
        });
        guildViewHolder.joinLeaveButton.setOnClickListener(v -> {
            Group guild = (Group) v.getTag();
            boolean isMember = this.memberGuildIDs != null && this.memberGuildIDs.contains(guild.id);
            if (isMember) {
                PublicGuildsRecyclerViewAdapter.this.apiClient.leaveGroup(guild.id)

                        .subscribe(aVoid -> {
                            memberGuildIDs.remove(guild.id);
                            int indexOfGroup = publicGuildList.indexOf(guild);
                            notifyItemChanged(indexOfGroup);
                        }, throwable -> {
                        });
            } else {
                PublicGuildsRecyclerViewAdapter.this.apiClient.joinGroup(guild.id)

                        .subscribe(group -> {
                            memberGuildIDs.add(group.id);
                            int indexOfGroup = publicGuildList.indexOf(group);
                            notifyItemChanged(indexOfGroup);
                        }, throwable -> {
                        });
            }

        });
        return guildViewHolder;
    }

    @Override
    public void onBindViewHolder(GuildViewHolder holder, int position) {
        Group guild = publicGuildList.get(position);
        boolean isInGroup = isInGroup(guild);
        holder.bind(guild, isInGroup);
        holder.itemView.setTag(guild);
        holder.joinLeaveButton.setTag(guild);
    }

    @Override
    public int getItemCount() {
        return this.publicGuildList == null ? 0 : this.publicGuildList.size();
    }

    private boolean isInGroup(Group guild) {
        return this.memberGuildIDs != null && this.memberGuildIDs.contains(guild.id);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Group> filteredGuilds;

                if (constraint.length() == 0) {
                    filteredGuilds = fullPublicGuildList;
                } else {
                    filteredGuilds = getFilteredResults(constraint.toString().toLowerCase(Locale.US));
                }

                FilterResults results = new FilterResults();
                results.values = filteredGuilds;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                publicGuildList = (List<Group>) results.values;
                PublicGuildsRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        };
    }

    protected List<Group> getFilteredResults(String query) {
        List<Group> filteredGuilds = new ArrayList<>();

        for (Group guild : fullPublicGuildList) {
            if (guild.name.toLowerCase().contains(query)) {
                filteredGuilds.add(guild);
            }
        }

        return filteredGuilds;
    }

    static class GuildViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.nameTextView)
        TextView nameTextView;

        @BindView(R.id.memberCountTextView)
        TextView memberCountTextView;

        @BindView(R.id.descriptionTextView)
        TextView descriptionTextView;

        @BindView(R.id.joinleaveButton)
        Button joinLeaveButton;

        public GuildViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Group guild, boolean isInGroup) {
            this.nameTextView.setText(guild.name);
            this.memberCountTextView.setText(String.valueOf(guild.memberCount));
            this.descriptionTextView.setText(guild.description);
            if (isInGroup) {
                this.joinLeaveButton.setText(R.string.leave);
            } else {
                this.joinLeaveButton.setText(R.string.join);
            }
        }
    }
}
