package com.habitrpg.android.habitica.ui.fragments.setup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.activities.SetupActivity;
import com.habitrpg.android.habitica.ui.adapter.setup.TaskSetupAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskSetupFragment extends BaseFragment {


    View view;
    public SetupActivity activity;
    public int width;

    private String[][] taskGroups;

    private Object[][] tasks;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    TaskSetupAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_setup_tasks, container, false);

        this.setTasks();

        unbinder = ButterKnife.bind(this, view);
        this.adapter = new TaskSetupAdapter();
        this.adapter.setTaskList(this.taskGroups);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        this.recyclerView.setAdapter(this.adapter);
        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void setTasks() {
        this.taskGroups = new String[][]{
                {getString(R.string.setup_group_work), "work"},
                {getString(R.string.setup_group_exercise), "exercise"},
                {getString(R.string.setup_group_heathWellness), "healthWellness"},
                {getString(R.string.setup_group_school), "school"},
                {getString(R.string.setup_group_teams), "teams"},
                {getString(R.string.setup_group_chores), "chores"},
                {getString(R.string.setup_group_creativity), "creativity"},
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

    public List<Map<String, Object>> createSampleTasks() {
        List<String> groups = new ArrayList<>();
        int i = 0;
        for (Boolean checked : this.adapter.checkedList) {
            if (checked) {
                groups.add(this.taskGroups[i][1]);
            }
            i++;
        }
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (Object[] task : this.tasks) {
            if (groups.contains((String) task[0])) {
                Map<String, Object> taskObject = new HashMap<>();
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

    private Map<String, Object> makeTaskObject(String type, String text, Boolean up, Boolean down) {
        Map<String, Object> task = new HashMap<>();
        task.put("text", text);
        task.put("priority", 1);
        task.put("type", type);

        if (type.equals("habit")) {
            task.put("up", up);
            task.put("down", down);
        }

        if (type.equals("daily")) {
            task.put("frequency", "weekly");
            task.put("startDate", new Date());
            Map<String, Boolean> repeat= new HashMap<>();
            repeat.put("m", true);
            repeat.put("t", true);
            repeat.put("w", true);
            repeat.put("th", true);
            repeat.put("f", true);
            repeat.put("s", true);
            repeat.put("su", true);
            task.put("repeat", repeat);
        }

        return task;
    }

}
