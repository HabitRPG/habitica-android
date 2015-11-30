package com.habitrpg.android.habitica.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.SkillTasksActivity;
import com.habitrpg.android.habitica.callbacks.SkillCallback;
import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.habitrpg.android.habitica.events.commands.UseSkillCommand;
import com.habitrpg.android.habitica.ui.adapter.SkillsRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by viirus on 25/11/15.
 */
public class SkillsFragment extends BaseFragment {

    private final int TASK_SELECTION_ACTIVITY = 10;

    private View view;
    private Skill selectedSkill;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_skills, container, false);

        adapter = new SkillsRecyclerViewAdapter();
        adapter.mana = this.user.getStats().getMp();
        loadSkills();

        return view;
    }

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    SkillsRecyclerViewAdapter adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setAdapter(adapter);
    }

    public void loadSkills() {
        List<Skill> skills = new Select()
                .from(Skill.class)
                .where(Condition.column("habitClass").eq(user.getStats().get_class()))
                .and(Condition.column("lvl").lessThanOrEq(user.getStats().getLvl()))
                .queryList();
        adapter.setSkillList(skills);
    }

    public void onEvent(UseSkillCommand command) {
        Skill skill = command.skill;
        if (skill.target.equals("task")) {
            selectedSkill = skill;
            Intent intent = new Intent(activity, SkillTasksActivity.class);
            startActivityForResult(intent, TASK_SELECTION_ACTIVITY);
        } else {
            mAPIHelper.apiService.useSkill(skill.key, skill.target, new SkillCallback(activity, skill));
        }
    }

    public void onEvent(SkillUsedEvent event) {
        Skill skill = event.usedSkill;
        adapter.setMana(event.newMana);
        activity.showSnackbar(activity.getString(R.string.used_skill, skill.text, skill.mana));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (TASK_SELECTION_ACTIVITY) : {
                if (resultCode == Activity.RESULT_OK) {
                    mAPIHelper.apiService.useSkill(selectedSkill.key, selectedSkill.target, data.getStringExtra("task_id"), new SkillCallback(activity, selectedSkill));
                }
                break;
            }
        }
    }
}
