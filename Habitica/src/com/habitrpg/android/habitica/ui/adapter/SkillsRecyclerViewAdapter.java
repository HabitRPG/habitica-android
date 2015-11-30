package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.CopyChatAsTodoCommand;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.OpenNewPMActivityCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.events.commands.UseSkillCommand;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.Iconics;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import de.greenrobot.event.EventBus;

/**
 * Created by viirus on 25/11/15.
 */
public class SkillsRecyclerViewAdapter extends RecyclerView.Adapter<SkillsRecyclerViewAdapter.SkillViewHolder> {


    public Double mana;
    private List<Skill> skillList;

    public void setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
        this.notifyDataSetChanged();
    }

    public void setMana(Double mana) {
        this.mana = mana;
        this.notifyDataSetChanged();
    }


    @Override
    public SkillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.skill_list_item, parent, false);

        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SkillViewHolder holder, int position) {
        holder.bind(skillList.get(position));
    }

    @Override
    public int getItemCount() {
        return skillList == null ? 0 : skillList.size();
    }

    class SkillViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(R.id.skill_text)
        TextView skillNameTextView;

        @InjectView(R.id.skill_notes)
        TextView skillNotesTextView;

        @InjectView(R.id.price_button)
        Button priceButton;

        Skill skill;

        Resources resources;

        public SkillViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);

            resources = itemView.getResources();

            priceButton.setOnClickListener(this);
        }

        public void bind(Skill skill) {
            this.skill = skill;
            skillNameTextView.setText(skill.text);
            skillNotesTextView.setText(skill.notes);
            priceButton.setText(String.format(resources.getString(R.string.mana_price_button), skill.mana));

            if (skill.mana > mana) {
                priceButton.setEnabled(false);
                priceButton.setBackgroundResource(R.color.task_gray);
                skillNameTextView.setTextColor(resources.getColor(R.color.task_gray));
                skillNotesTextView.setTextColor(resources.getColor(R.color.task_gray));
            } else {
                skillNameTextView.setTextColor(resources.getColor(android.R.color.black));
                skillNotesTextView.setTextColor(resources.getColor(android.R.color.black));
                priceButton.setEnabled(true);
            }
        }

        @Override
        public void onClick(View v) {
            UseSkillCommand event = new UseSkillCommand();
            event.skill = this.skill;

            EventBus.getDefault().post(event);
        }
    }
}
