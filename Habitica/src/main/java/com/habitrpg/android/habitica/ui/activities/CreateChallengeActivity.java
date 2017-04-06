package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeTasksRecyclerViewFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;

public class CreateChallengeActivity extends BaseActivity {

    @BindView(R.id.challenge_location_spinner)
    Spinner challenge_location_spinner;


    @BindView(R.id.create_challenge_task_list)
    RecyclerView create_challenge_task_list;

    private boolean displayingTaskForm;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_create_challenge;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_challenge, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        Resources resources = getResources();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setIcon(R.drawable.ic_close_white_24dp);

            supportActionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.brand_200)));
            supportActionBar.setElevation(0);
        }

        ArrayAdapter<CharSequence> locationAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationAdapter.addAll("Tavern", "My Group", "Some other");
        challenge_location_spinner.setAdapter(locationAdapter);

        Task addHabit = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_habit));
        Task addDaily = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_daily));
        Task addTodo = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_todo));
        Task addReward = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_reward));

        ArrayList<Task> taskList = new ArrayList<>();
        taskList.add(addHabit);
        taskList.add(createTask(Task.TYPE_HABIT));
        taskList.add(addDaily);
        taskList.add(createTask(Task.TYPE_DAILY));
        taskList.add(addTodo);
        taskList.add(createTask(Task.TYPE_TODO));
        taskList.add(addReward);
        taskList.add(createTask(Task.TYPE_REWARD));

        ChallengeTasksRecyclerViewAdapter challengeTasks = new ChallengeTasksRecyclerViewAdapter(null, 0, this, "", null, false, true);
        challengeTasks.setTasks(taskList);
        challengeTasks.enableAddItem(t -> {
            if (t.equals(addHabit)) {
                openNewTaskActivity(Task.TYPE_HABIT);
            } else if (t.equals(addDaily)) {

                openNewTaskActivity(Task.TYPE_DAILY);
            } else if (t.equals(addTodo)) {

                openNewTaskActivity(Task.TYPE_TODO);
            } else if (t.equals(addReward)) {

                openNewTaskActivity(Task.TYPE_REWARD);
            }
        });

        create_challenge_task_list.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                // Stop only scrolling.
                return rv.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;
            }
        });
        create_challenge_task_list.setAdapter(challengeTasks);
        create_challenge_task_list.setLayoutManager(new LinearLayoutManager(this));

    }

    private void openNewTaskActivity(String type) {
        if (this.displayingTaskForm) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type);
        bundle.putBoolean(TaskFormActivity.SAVE_TO_DB, false);

        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null) {
            String allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();

            bundle.putString(TaskFormActivity.USER_ID_KEY, HabiticaApplication.User.getId());
            bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);
        }


        Intent intent = new Intent(this, TaskFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        this.displayingTaskForm = true;
        startActivityForResult(intent, 1);
    }

    @Subscribe
    public void onEvent(TaskSaveEvent saveEvent)
    {

    }

    private Task createTask(String taskType) {
        return createTask(taskType, "example " + taskType);
    }

    private Task createTask(String taskType, String taskName) {
        Task t = new Task();

        t.setId(UUID.randomUUID().toString());
        t.setType(taskType);
        t.setText(taskName);
        t.setNotes("example " + taskType + " notes");

        if (taskType.equals(Task.TYPE_HABIT)) {
            t.setUp(true);
            t.setDown(false);
        }

        return t;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }
}
