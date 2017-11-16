package com.habitrpg.android.habitica.ui.fragments.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.models.tasks.Days;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.SpeechBubbleView;
import com.habitrpg.android.habitica.ui.activities.SetupActivity;
import com.habitrpg.android.habitica.ui.adapter.setup.TaskSetupAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskSetupFragment extends BaseFragment {


    public SetupActivity activity;
    public int width;
    View view;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.avatarView)
    AvatarView avatarView;
    @BindView(R.id.speech_bubble)
    SpeechBubbleView speechBubbleView;
    TaskSetupAdapter adapter;
    private String[][] taskGroups;
    private Object[][] tasks;
    private User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_setup_tasks, container, false);

        this.setTasks();

        setUnbinder(ButterKnife.bind(this, view));
        this.adapter = new TaskSetupAdapter();
        this.adapter.setTaskList(this.taskGroups);
        this.recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        this.recyclerView.setAdapter(this.adapter);

        if (this.user != null) {
            this.updateAvatar();
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getContext() != null) {
            speechBubbleView.animateText(getContext().getString(R.string.task_setup_description));
        }
    }

    public void setUser(@Nullable User user) {
        this.user = user;
        if (avatarView != null) {
            updateAvatar();
        }
    }

    private void updateAvatar() {
        avatarView.setAvatar(user);
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void setTasks() {
        this.taskGroups = new String[][]{
                {getString(R.string.setup_group_work), "work"},
                {getString(R.string.setup_group_exercise), "exercise"},
                {getString(R.string.setup_group_health), "healthWellness"},
                {getString(R.string.setup_group_school), "school"},
                {getString(R.string.setup_group_teams), "teams"},
                {getString(R.string.setup_group_chores), "chores"},
                {getString(R.string.setup_group_creativity), "creativity"},
                {getString(R.string.setuP_group_other), "other"}
        };

        this.tasks = new Object[][]{
                {"work", "habit", getString(R.string.setup_task_work_1), true, false},
                {"work", "daily", getString(R.string.setup_task_work_2)},
                {"work", "todo", getString(R.string.setup_task_work_3)},

                {"exercise", "habit", getString(R.string.setup_task_exercise_1), true, false},
                {"exercise", "daily", getString(R.string.setup_task_exercise_2)},
                {"exercise", "todo", getString(R.string.setup_task_exercise_3)},

                {"healthWellness", "habit", getString(R.string.setup_task_healthWellness_1), true, true},
                {"healthWellness", "daily", getString(R.string.setup_task_healthWellness_2)},
                {"healthWellness", "todo", getString(R.string.setup_task_healthWellness_3)},

                {"school", "habit", getString(R.string.setup_task_school_1), true, true},
                {"school", "daily", getString(R.string.setup_task_school_2)},
                {"school", "todo", getString(R.string.setup_task_school_3)},

                {"teams", "habit", getString(R.string.setup_task_teams_1), true, false},
                {"teams", "daily", getString(R.string.setup_task_teams_2)},
                {"teams", "todo", getString(R.string.setup_task_teams_3)},

                {"chores", "habit", getString(R.string.setup_task_chores_1), true, false},
                {"chores", "daily", getString(R.string.setup_task_chores_2)},
                {"chores", "todo", getString(R.string.setup_task_chores_3)},

                {"creativity", "habit", getString(R.string.setup_task_creativity_1), true, false},
                {"creativity", "daily", getString(R.string.setup_task_creativity_2)},
                {"creativity", "todo", getString(R.string.setup_task_creativity_3)},
        };
    }

    public List<Task> createSampleTasks() {
        List<String> groups = new ArrayList<>();
        int i = 0;
        for (Boolean checked : this.adapter.checkedList) {
            if (checked) {
                groups.add(this.taskGroups[i][1]);
            }
            i++;
        }
        List<Task> tasks = new ArrayList<>();
        for (Object[] task : this.tasks) {
            String taskGroup = (String) task[0];
            if (groups.contains(taskGroup)) {
                Task taskObject;
                if (task.length == 5) {
                    taskObject = this.makeTaskObject((String) task[1], (String) task[2], (Boolean) task[3], (Boolean) task[4]);
                } else {
                    taskObject = this.makeTaskObject((String) task[1], (String) task[2], null, null);
                }
                tasks.add(taskObject);
            }
        }
        return tasks;
    }

    private Task makeTaskObject(String type, String text, @Nullable Boolean up, @Nullable Boolean down) {
        Task task = new Task();
        task.setText(text);
        task.setPriority(1.0f);
        task.setType(type);

        if (type.equals("habit")) {
            task.setUp(up);
            task.setDown(down);
        }

        if (type.equals("daily")) {
            task.setFrequency("weekly");
            task.setStartDate(new Date());
            Days days = new Days();
            days.setM(true);
            days.setT(true);
            days.setW(true);
            days.setTh(true);
            days.setF(true);
            days.setS(true);
            days.setSu(true);
            task.setRepeat(days);
        }

        return task;
    }

}
