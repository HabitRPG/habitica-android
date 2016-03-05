package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
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

        return new GuildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuildViewHolder holder, int position) {
        holder.bind(publicGuildList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.publicGuildList == null ? 0 : this.publicGuildList.size();
    }

    class GuildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Callback<Group> {

        @Bind(R.id.nameTextView)
        TextView nameTextView;

        @Bind(R.id.memberCountTextView)
        TextView memberCountTextView;

        @Bind(R.id.descriptionTextView)
        TextView descriptionTextView;

        @Bind(R.id.joinleaveButton)
        Button joinLeaveButton;

        Group guild;
        Boolean isMember;

        public GuildViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            joinLeaveButton.setOnClickListener(this);
        }

        public void bind(Group guild) {
            android.content.Context ctx = itemView.getContext();
            this.guild = guild;
            this.nameTextView.setText(guild.name);
            this.memberCountTextView.setText(String.valueOf(guild.memberCount));
            this.descriptionTextView.setText(guild.description);
            if (PublicGuildsRecyclerViewAdapter.this.memberGuildIDs.contains(guild.id)) {
                this.isMember = true;
                this.joinLeaveButton.setText(R.string.leave);
            } else {
                this.isMember = false;
                this.joinLeaveButton.setText(R.string.join);
            }
        }

        @Override
        public void onClick(View v) {
            if (v == this.joinLeaveButton) {
                if (this.isMember) {
                    PublicGuildsRecyclerViewAdapter.this.apiHelper.apiService.leaveGroup(this.guild.id, this);
                } else {
                    PublicGuildsRecyclerViewAdapter.this.apiHelper.apiService.joinGroup(this.guild.id, this);
                }
            } else {
                GuildFragment guildFragment = new GuildFragment();
                guildFragment.setGuild(this.guild);
                guildFragment.isMember = this.isMember;
                DisplayFragmentEvent event = new DisplayFragmentEvent();
                event.fragment = guildFragment;
                EventBus.getDefault().post(event);
            }
        }

        @Override
        public void success(Group group, Response response) {
            this.bind(guild);
        }

        @Override
        public void failure(RetrofitError error) {

        }
    }
}
