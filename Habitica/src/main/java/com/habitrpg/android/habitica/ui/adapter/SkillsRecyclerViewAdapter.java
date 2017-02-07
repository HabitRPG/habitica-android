package com.habitrpg.android.habitica.ui.adapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.UseSkillCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        @BindView(R.id.skill_image)
        SimpleDraweeView skillImageView;

        @BindView(R.id.skill_text)
        TextView skillNameTextView;

        @BindView(R.id.skill_notes)
        TextView skillNotesTextView;

        @BindView(R.id.price_button)
        Button priceButton;

        Skill skill;

        Context context;

        public SkillViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            context = itemView.getContext();

            priceButton.setOnClickListener(this);
        }

        public void bind(Skill skill) {
            this.skill = skill;
            skillNameTextView.setText(skill.text);
            skillNotesTextView.setText(skill.notes);

            if (skill.isSpecialItem) {
                priceButton.setText(R.string.skill_transformation_use);

                priceButton.setCompoundDrawables(null, null, null, null);
            } else {
                priceButton.setText(skill.mana + "");
                priceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_header_magic, 0, 0, 0);
            }
            DataBindingUtils.loadImage(skillImageView, "shop_" + skill.key);

            if (skill.mana > mana) {
                priceButton.setEnabled(false);
                priceButton.setBackgroundResource(R.color.task_gray);
                skillNameTextView.setTextColor(ContextCompat.getColor(context, R.color.task_gray));
                skillNotesTextView.setTextColor(ContextCompat.getColor(context, R.color.task_gray));
            } else {
                skillNameTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                skillNotesTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
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
