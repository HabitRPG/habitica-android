package com.habitrpg.android.habitica.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Negue on 22.09.2015.
 */
public class PartyMemberQuestRecyclerViewAdapter extends RecyclerView.Adapter<PartyMemberQuestRecyclerViewAdapter.MemberQuestViewHolder> {

    Group group;

    public void setGroup(Group group) {
        Log.d("PartyMemberQuestFrag", "set group");
        this.group = group;
        this.notifyDataSetChanged();
    }

    @Override
    public MemberQuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.party_member_quest, parent, false);
        Log.d("PartyMemberQuestFrag", "inflated");

        return new MemberQuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberQuestViewHolder holder, int position) {
        Log.d("PartyMemberQuestFrag", "call binder");

        holder.bind(group.members.get(position));
    }

    @Override
    public int getItemCount() {

        Log.d("PartyMemberQuestFrag", "group " + ((group == null)? 0 : group.memberCount));
        return (group == null)? 0 : group.memberCount;
    }

    class MemberQuestViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.username)
        TextView userName;

        @Bind(R.id.rsvpneeded)
        TextView rsvpNeeded;


        public MemberQuestViewHolder(View itemView) {
            super(itemView);

            Log.d("PartyMemberQuestFrag", "VH init");

            ButterKnife.bind(this, itemView);

            Log.d("PartyMemberFrag", "VH init");
        }

        public void bind(HabitRPGUser user) {
            Log.d("PartyMemberQuestFrag", "bind " + user.getProfile().getName());
            android.content.Context ctx = itemView.getContext();

            userName.setText(user.getProfile().getName());

            Boolean rsvpneeded = group.quest.members.get(user.getId());
            if(rsvpneeded == null){
                rsvpNeeded.setText("Pending");
            }else if(rsvpneeded == true){
                rsvpNeeded.setText("Accepted");
            }else{ // rsvpneeded == false
                rsvpNeeded.setText("Rejected");
            }
        }
    }
}
