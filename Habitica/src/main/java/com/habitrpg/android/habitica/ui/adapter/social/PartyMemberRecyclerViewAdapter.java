package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.SelectMemberCommand;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.habitrpg.android.habitica.ui.views.ValueBar;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class PartyMemberRecyclerViewAdapter extends RealmRecyclerViewAdapter<User, PartyMemberRecyclerViewAdapter.MemberViewHolder> {


    private Context context;
    private boolean isMemberSelection;

    public PartyMemberRecyclerViewAdapter(@Nullable OrderedRealmCollection<User> data, boolean autoUpdate, Context context, boolean isMemberSelection) {
        super(data, autoUpdate);
        this.context = context;
        this.isMemberSelection = isMemberSelection;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.party_member, parent, false);

        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        if (getData() != null) {
            holder.bind(getData().get(position));
        }
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.avatarView)
        AvatarView avatarView;

        @BindView(R.id.username)
        TextView userName;

        @BindView(R.id.user_lvl)
        TextView lvl;

        @BindView(R.id.class_label)
        TextView classLabel;

        @BindView(R.id.class_background_layout)
        View classBackground;

        @BindView(R.id.hpBar)
        ValueBar hpBar;

        Resources resources;

        MemberViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            hpBar.setLightBackground(true);

            resources = itemView.getResources();
        }

        public void bind(User user) {
            avatarView.setUser(user);

            AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats());

            lvl.setText(context.getString(R.string.user_level, user.getStats().getLvl()));

            classLabel.setText(user.getStats().getTranslatedClassName(context));

            int colorResourceID;
            switch (user.getStats().habitClass) {
                case "healer": {
                    colorResourceID = R.color.class_healer;
                    break;
                }
                case "warrior": {
                    colorResourceID = R.color.class_warrior;
                    break;
                }
                case "rogue": {
                    colorResourceID = R.color.class_rogue;
                    break;
                }
                case "wizard": {
                    colorResourceID = R.color.class_wizard;
                    break;
                }
                default:
                    colorResourceID = R.color.task_gray;
            }
            ViewHelper.SetBackgroundTint(classBackground, ContextCompat.getColor(context, colorResourceID));
            userName.setText(user.getProfile().getName());

            itemView.setClickable(true);
            itemView.setOnClickListener(view -> {
                if (isMemberSelection) {
                    SelectMemberCommand cmd = new SelectMemberCommand(user.getId());

                    EventBus.getDefault().post(cmd);
                } else {
                    OpenFullProfileCommand cmd = new OpenFullProfileCommand(user.getId());

                    EventBus.getDefault().post(cmd);
                }
            });
        }


    }
}
