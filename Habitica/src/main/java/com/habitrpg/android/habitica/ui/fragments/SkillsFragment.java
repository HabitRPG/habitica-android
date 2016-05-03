package com.habitrpg.android.habitica.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.SkillCallback;
import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.habitrpg.android.habitica.events.commands.UseSkillCommand;
import com.habitrpg.android.habitica.ui.UiUtils;
import com.habitrpg.android.habitica.ui.activities.SkillTasksActivity;
import com.habitrpg.android.habitica.ui.adapter.SkillsRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SkillsFragment extends BaseMainFragment {

    private final int TASK_SELECTION_ACTIVITY = 10;

    private View view;
    private Skill selectedSkill;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_skills, container, false);

        adapter = new SkillsRecyclerViewAdapter();
        checkUserLoadSkills();

        this.tutorialStepIdentifier = "skills";
        this.tutorialText = getString(R.string.tutorial_skills);

        return view;
    }

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    SkillsRecyclerViewAdapter adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setAdapter(adapter);
    }

    private void checkUserLoadSkills(){
        if(user == null || adapter == null){
            return;
        }

        adapter.mana = this.user.getStats().getMp();

        List<Skill> skills = new Select()
                .from(Skill.class)
                .where(Condition.column("habitClass").eq(user.getStats().get_class()))
                .and(Condition.column("lvl").lessThanOrEq(user.getStats().getLvl()))
                .queryList();
        adapter.setSkillList(skills);
    }

    @Override
    public void setUser(HabitRPGUser user) {
        super.setUser(user);

        checkUserLoadSkills();
    }

    @Subscribe
    public void onEvent(UseSkillCommand command) {
        Skill skill = command.skill;
        if (skill.target.equals("task")) {
            selectedSkill = skill;
            Intent intent = new Intent(activity, SkillTasksActivity.class);
            startActivityForResult(intent, TASK_SELECTION_ACTIVITY);
        } else {
            displayProgressDialog();
            mAPIHelper.apiService.useSkill(skill.key, skill.target, new SkillCallback(activity, skill));
        }
    }

    @Subscribe
    public void onEvent(SkillUsedEvent event) {
        removeProgressDialog();
        Skill skill = event.usedSkill;
        adapter.setMana(event.newMana);
        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), activity.getString(R.string.used_skill, skill.text, skill.mana), UiUtils.SnackbarDisplayType.NORMAL);
        mAPIHelper.retrieveUser(new HabitRPGUserCallback(activity));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (TASK_SELECTION_ACTIVITY) : {
                if (resultCode == Activity.RESULT_OK) {
                    displayProgressDialog();
                    mAPIHelper.apiService.useSkill(selectedSkill.key, selectedSkill.target, data.getStringExtra("task_id"), new SkillCallback(activity, selectedSkill));
                }
                break;
            }
        }
    }

    private void displayProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(activity, activity.getString(R.string.skill_progress_title), null, true);
    }

    private void removeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);

    }

}
