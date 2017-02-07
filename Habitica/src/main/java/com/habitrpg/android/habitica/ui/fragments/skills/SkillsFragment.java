package com.habitrpg.android.habitica.ui.fragments.skills;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.MergeUserCallback;
import com.habitrpg.android.habitica.callbacks.SkillCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.habitrpg.android.habitica.events.commands.UseSkillCommand;
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity;
import com.habitrpg.android.habitica.ui.activities.SkillTasksActivity;
import com.habitrpg.android.habitica.ui.adapter.SkillsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.SpecialItems;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.SkillResponse;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.Subscribe;

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

import java.util.List;

import butterknife.BindView;
import rx.Observable;

import static com.habitrpg.android.habitica.helpers.MathHelper.round;

public class SkillsFragment extends BaseMainFragment {

    private final int TASK_SELECTION_ACTIVITY = 10;
    private final int MEMBER_SELECTION_ACTIVITY = 11;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    SkillsRecyclerViewAdapter adapter;
    private View view;
    private Skill selectedSkill;
    private ProgressDialog progressDialog;

    static public Double round(Double value, int n) {
        return (Math.round(value * Math.pow(10, n))) / (Math.pow(10, n));
    }

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

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setAdapter(adapter);
    }

    private void checkUserLoadSkills() {
        if (user == null || adapter == null) {
            return;
        }

        adapter.mana = this.user.getStats().getMp();

        List<Skill> skills = new Select()
                .from(Skill.class)
                .where(Condition.column("habitClass").eq(user.getStats().get_class()))
                .and(Condition.column("lvl").lessThanOrEq(user.getStats().getLvl()))
                .queryList();
        adapter.setSkillList(skills);


        SpecialItems specialItems = this.user.getItems().getSpecial();
        if (specialItems != null) {
            Condition.In specialsWhere = Condition.column("key").in("");

            if (specialItems.getSnowball() > 0) {
                specialsWhere.and("snowball");
            }

            if (specialItems.getShinySeed() > 0) {
                specialsWhere.and("shinySeed");
            }

            if (specialItems.getSeafoam() > 0) {
                specialsWhere.and("seafoam");
            }

            if (specialItems.getSpookySparkles() > 0) {
                specialsWhere.and("spookySparkles");
            }

            List<Skill> specials = new Select()
                    .from(Skill.class)
                    .where(specialsWhere)
                    .queryList();

            for (Skill item : specials) {
                item.isSpecialItem = true;
                item.target = "party";

                skills.add(item);
            }
        }

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

        if (skill.isSpecialItem) {
            selectedSkill = skill;
            Intent intent = new Intent(activity, SkillMemberActivity.class);
            startActivityForResult(intent, MEMBER_SELECTION_ACTIVITY);
        } else if (skill.target.equals("task")) {
            selectedSkill = skill;
            Intent intent = new Intent(activity, SkillTasksActivity.class);
            startActivityForResult(intent, TASK_SELECTION_ACTIVITY);
        } else {
            useSkill(skill);
        }
    }

    @Subscribe
    public void onEvent(SkillUsedEvent event) {
        removeProgressDialog();
        Skill skill = event.usedSkill;
        adapter.setMana(event.newMana);
        StringBuilder message = new StringBuilder();
        if (skill.isSpecialItem) {
            message.append(activity.getString(R.string.used_skill_without_mana, skill.text));
        } else {
            message.append(activity.getString(R.string.used_skill, skill.text, skill.mana));
        }

        if (event.xp != 0) {
            message.append(" + ").append(round(event.xp, 2)).append(" XP");
        }
        if (event.hp != 0) {
            message.append(" + ").append(round(event.hp, 2)).append(" HP");
        }
        if (event.gold != 0) {
            message.append(" + ").append(round(event.gold, 2)).append(" GP");
        }
        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), message.toString(), UiUtils.SnackbarDisplayType.NORMAL);
        apiClient.getUser()

                .subscribe(new MergeUserCallback(activity, user), throwable -> {
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (TASK_SELECTION_ACTIVITY): {
                if (resultCode == Activity.RESULT_OK) {
                    useSkill(selectedSkill, data.getStringExtra("task_id"));
                }
                break;
            }
            case (MEMBER_SELECTION_ACTIVITY): {
                if (resultCode == Activity.RESULT_OK) {
                    useSkill(selectedSkill, data.getStringExtra("member_id"));
                }
                break;
            }
        }
    }

    private void useSkill(Skill skill) {
        useSkill(skill, null);
    }

    private void useSkill(Skill skill, String taskId) {
        displayProgressDialog();
        Observable<SkillResponse> observable;
        if (taskId != null) {
            observable = apiClient.useSkill(skill.key, skill.target, taskId);
        } else {
            observable = apiClient.useSkill(skill.key, skill.target);
        }
        observable
                .subscribe(new SkillCallback(activity, user, skill), throwable -> {
                    removeProgressDialog();
                });
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
	public String customTitle() {	return getString(R.string.sidebar_skills);	}

}
