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

import butterknife.ButterKnife;
import butterknife.InjectView;

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

        @InjectView(R.id.avatar)
        ImageView imageView;

        @InjectView(R.id.username)
        TextView userName;

        @InjectView(R.id.user_lvl)
        TextView lvl;

        @InjectView(R.id.class_label)
        TextView classLabel;

        @InjectView(R.id.class_background_layout)
        View classBackground;

        ValueBarBinding hpBar;

        Resources resources;

        public MemberViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);

            View hpBarView = itemView.findViewById(R.id.hpBar);

            hpBar = DataBindingUtil.bind(hpBarView);

            resources = itemView.getResources();
        }

        public void bind(HabitRPGUser user) {
            android.content.Context ctx = itemView.getContext();

            UserPicture userPicture = new UserPicture(user, ctx);
            userPicture.setPictureOn(imageView);

            AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats(), ctx);

            lvl.setText("LVL " + user.getStats().getLvl());

            classLabel.setText(user.getStats()._class.toString());


            switch (user.getStats()._class) {
                case healer: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.class_healer));
                    break;
                }
                case warrior: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.class_warrior));
                    break;
                }
                case rogue: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.class_rogue));
                    break;
                }
                case wizard: {
                    ViewHelper.SetBackgroundTint(classBackground, resources.getColor(R.color.class_wizard));
                    break;
                }
            }

            userName.setText(user.getProfile().getName());
        }
    }
}
