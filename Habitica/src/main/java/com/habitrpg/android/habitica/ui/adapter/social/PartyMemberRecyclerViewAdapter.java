package com.habitrpg.android.habitica.ui.adapter.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PartyMemberRecyclerViewAdapter extends RecyclerView.Adapter<PartyMemberRecyclerViewAdapter.MemberViewHolder> {


    private List<HabitRPGUser> memberList;
    public Context context;
    public void setMemberList(List<HabitRPGUser> memberList) {
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

        @BindView(R.id.avatar)
        ImageView imageView;

        @BindView(R.id.username)
        TextView userName;

        @BindView(R.id.user_lvl)
        TextView lvl;

        @BindView(R.id.class_label)
        TextView classLabel;

        @BindView(R.id.class_background_layout)
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
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            if (dpWidth >= 320) {
                userPicture = new UserPicture(itemView.getContext(), true, true);
            } else {
                userPicture = new UserPicture(itemView.getContext(), false, false);
            }
        }

        public void bind(HabitRPGUser user) {
            android.content.Context ctx = itemView.getContext();

            userPicture.setUser(user);
            userPicture.setPictureOn(imageView);

            AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats(), ctx);

            lvl.setText(context.getString(R.string.user_level, user.getStats().getLvl()));

            classLabel.setText(user.getStats().getCleanedClassName());

            int colorResourceID;
            switch (user.getStats()._class) {
                case healer: {
                    colorResourceID = R.color.class_healer;
                    break;
                }
                case warrior: {
                    colorResourceID = R.color.class_warrior;
                    break;
                }
                case rogue: {
                    colorResourceID = R.color.class_rogue;
                    break;
                }
                case wizard: {
                    colorResourceID = R.color.class_wizard;
                    break;
                }
                default:
                    colorResourceID = R.color.task_gray;
            }
            ViewHelper.SetBackgroundTint(classBackground, ContextCompat.getColor(context, colorResourceID));
            userName.setText(user.getProfile().getName());
        }
    }
}
