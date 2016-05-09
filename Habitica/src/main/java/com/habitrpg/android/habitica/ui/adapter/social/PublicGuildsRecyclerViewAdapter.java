package com.habitrpg.android.habitica.ui.adapter.social;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PublicGuildsRecyclerViewAdapter extends RecyclerView.Adapter<PublicGuildsRecyclerViewAdapter.GuildViewHolder> {

    public APIHelper apiHelper;
    private ArrayList<Group> publicGuildList;
    private ArrayList<String> memberGuildIDs;

    public void setPublicGuildList(ArrayList<Group> publicGuildList) {
        this.publicGuildList = publicGuildList;
        this.notifyDataSetChanged();
    }

    public void setMemberGuildIDs(ArrayList<String> memberGuildIDs) {
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
                PublicGuildsRecyclerViewAdapter.this.apiHelper.apiService.leaveGroup(guild.id, new Callback<Void>() {
                    @Override
                    public void success(Void nope, Response response) {
                        memberGuildIDs.remove(guild.id);
                        int indexOfGroup = publicGuildList.indexOf(guild);
                        notifyItemChanged(indexOfGroup);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(guildViewHolder.itemView.getContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            } else {
                PublicGuildsRecyclerViewAdapter.this.apiHelper.apiService.joinGroup(guild.id, new Callback<Group>() {
                    @Override
                    public void success(Group group, Response response) {
                        memberGuildIDs.add(group.id);
                        int indexOfGroup = publicGuildList.indexOf(group);
                        notifyItemChanged(indexOfGroup);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(guildViewHolder.itemView.getContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                                .show();
                    }
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
