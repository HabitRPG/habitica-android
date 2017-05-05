package com.habitrpg.android.habitica.ui.adapter.social;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Case;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class PublicGuildsRecyclerViewAdapter extends RealmRecyclerViewAdapter<Group, PublicGuildsRecyclerViewAdapter.GuildViewHolder> implements Filterable {

    public ApiClient apiClient;
    private List<String> memberGuildIDs;

    public PublicGuildsRecyclerViewAdapter(@Nullable OrderedRealmCollection<Group> data, boolean autoUpdate) {
        super(data, autoUpdate);
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
            guildFragment.setGuildId(guild.id);
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
                            int indexOfGroup = getData().indexOf(guild);
                            notifyItemChanged(indexOfGroup);
                        }, throwable -> {
                        });
            } else {
                PublicGuildsRecyclerViewAdapter.this.apiClient.joinGroup(guild.id)
                        .subscribe(group -> {
                            memberGuildIDs.add(group.id);
                            int indexOfGroup = getData().indexOf(group);
                            notifyItemChanged(indexOfGroup);
                        }, throwable -> {
                        });
            }

        });
        return guildViewHolder;
    }

    @Override
    public void onBindViewHolder(GuildViewHolder holder, int position) {
        Group guild = getData().get(position);
        boolean isInGroup = isInGroup(guild);
        holder.bind(guild, isInGroup);
        holder.itemView.setTag(guild);
        holder.joinLeaveButton.setTag(guild);
    }

    private boolean isInGroup(Group guild) {
        return this.memberGuildIDs != null && this.memberGuildIDs.contains(guild.id);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = constraint;
                return new FilterResults();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (getData() != null && constraint.length() > 0) {
                    updateData(getData().where()
                            .contains("name", String.valueOf(constraint), Case.INSENSITIVE)
                            .findAll());
                }
            }
        };
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

        GuildViewHolder(View itemView) {
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
