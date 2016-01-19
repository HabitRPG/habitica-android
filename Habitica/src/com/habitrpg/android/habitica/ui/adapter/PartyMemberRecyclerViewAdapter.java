package com.habitrpg.android.habitica.ui.adapter;

import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Negue on 22.09.2015.
 */
public class PartyMemberRecyclerViewAdapter extends RecyclerView.Adapter<PartyMemberRecyclerViewAdapter.MemberViewHolder> {


    private ArrayList<HabitRPGUser> memberList;

    public void setMemberList(ArrayList<HabitRPGUser> memberList) {
        this.memberList = memberList;
        this.notifyDataSetChanged();
    }


    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.party_member, parent, false);

        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        holder.bind(memberList.get(position));
    }

    @Override
    public int getItemCount() {
        return memberList == null ? 0 : memberList.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.avatar)
        ImageView imageView;

        @Bind(R.id.username)
        TextView userName;

        @Bind(R.id.user_lvl)
        TextView lvl;

        @Bind(R.id.class_label)
        TextView classLabel;

        @Bind(R.id.class_background_layout)
        View classBackground;

        ValueBarBinding hpBar;

        Resources resources;
        UserPicture userPicture;

        public MemberViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            View hpBarView = itemView.findViewById(R.id.hpBar);

            hpBar = DataBindingUtil.bind(hpBarView);
            hpBar.setPartyMembers(true);

            resources = itemView.getResources();
            userPicture = new UserPicture(itemView.getContext(), false, false);

        }

        public void bind(HabitRPGUser user) {
            android.content.Context ctx = itemView.getContext();

            userPicture.setUser(user);
            userPicture.setPictureOn(imageView);

            AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats(), ctx);

            lvl.setText("LVL " + user.getStats().getLvl());

            classLabel.setText(user.getStats().getCleanedClassName());


            switch (user.getStats()._class) {
                case healer: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.neutral_100));
                    break;
                }
                case warrior: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.worse_100));
                    break;
                }
                case rogue: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.brand_50));
                    break;
                }
                case wizard: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.best_100));
                    break;
                }
            }

            userName.setText(user.getProfile().getName());
        }
    }
}
