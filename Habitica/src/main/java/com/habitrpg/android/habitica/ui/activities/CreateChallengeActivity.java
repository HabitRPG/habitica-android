package com.habitrpg.android.habitica.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.DeleteTaskCommand;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.PostChallenge;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class CreateChallengeActivity extends BaseActivity {

    @BindView(R.id.create_challenge_title)
    EditText create_challenge_title;

    @BindView(R.id.create_challenge_description)
    EditText create_challenge_description;

    @BindView(R.id.create_challenge_prize)
    EditText create_challenge_prize;

    @BindView(R.id.create_challenge_tag)
    EditText create_challenge_tag;

    @BindView(R.id.challenge_location_spinner)
    Spinner challenge_location_spinner;


    @BindView(R.id.create_challenge_task_list)
    RecyclerView create_challenge_task_list;

    @Inject
    ChallengeLocalRepository challengeLocalRepository;

    @Inject
    ChallengeRepository challengeRepository;

    private ChallengeTasksRecyclerViewAdapter challengeTasks;

    Task addHabit;
    Task addDaily;
    Task addTodo;
    Task addReward;
    private GroupArrayAdapter locationAdapter;


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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveChallenge();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveChallenge() {
        PostChallenge c = new PostChallenge();

        int locationPos = challenge_location_spinner.getSelectedItemPosition();
        Group locationGroup = locationAdapter.getItem(locationPos);

        c.group = locationGroup.id;
        c.name = create_challenge_title.getText().toString();
        c.description = create_challenge_description.getText().toString();
        c.shortName = create_challenge_tag.getText().toString();
        c.prize = Integer.parseInt(create_challenge_prize.getText().toString());

        List<Task> taskList = challengeTasks.getTaskList();
        taskList.remove(addHabit);
        taskList.remove(addDaily);
        taskList.remove(addTodo);
        taskList.remove(addReward);

        challengeRepository.createChallenge(c, taskList).subscribe(createdChallenge -> {
            finish();
        }, throwable -> {
            // UHOH
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        Resources resources = getResources();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setIcon(R.drawable.ic_close_white_24dp);

            supportActionBar.setTitle(R.string.create_challenge);
            supportActionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.brand_200)));
            supportActionBar.setElevation(0);
        }

        locationAdapter = new GroupArrayAdapter(this);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        challengeLocalRepository.getGroups().subscribe(groups -> {
            Group tavern = new Group();
            tavern.id = "00000000-0000-4000-A000-000000000000";
            tavern.name = getString(R.string.tavern);

            locationAdapter.add(tavern);

            groups.forEach(group -> locationAdapter.add(group));
        }, Throwable::printStackTrace);

        challenge_location_spinner.setAdapter(locationAdapter);

        addHabit = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_habit));
        addDaily = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_daily));
        addTodo = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_todo));
        addReward = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_reward));


        ArrayList<Task> taskList = new ArrayList<>();
        taskList.add(addHabit);
        taskList.add(addDaily);
        taskList.add(addTodo);
        taskList.add(addReward);

        challengeTasks = new ChallengeTasksRecyclerViewAdapter(null, 0, this, "", null, false, true);
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
        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type);
        bundle.putBoolean(TaskFormActivity.SAVE_TO_DB, false);
        bundle.putBoolean(TaskFormActivity.SET_IGNORE_FLAG, true);
        bundle.putBoolean(TaskFormActivity.SHOW_TAG_SELECTION, false);

        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null) {
            String allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();

            bundle.putString(TaskFormActivity.USER_ID_KEY, HabiticaApplication.User.getId());
            bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);
        }

        Intent intent = new Intent(this, TaskFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivityForResult(intent, 1);
    }

    private void openNewTaskActivity(Task task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TaskFormActivity.PARCELABLE_TASK, task);
        bundle.putBoolean(TaskFormActivity.SAVE_TO_DB, false);
        bundle.putBoolean(TaskFormActivity.SET_IGNORE_FLAG, true);
        bundle.putBoolean(TaskFormActivity.SHOW_TAG_SELECTION, false);

        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null) {
            String allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();

            bundle.putString(TaskFormActivity.USER_ID_KEY, HabiticaApplication.User.getId());
            bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);
        }

        Intent intent = new Intent(this, TaskFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivityForResult(intent, 1);
    }

    @Subscribe
    public void onEvent(DeleteTaskCommand deleteTask) {
        challengeTasks.removeTask(deleteTask.TaskIdToDelete);
    }

    @Subscribe
    public void onEvent(TaskTappedEvent tappedEvent) {
        openNewTaskActivity(tappedEvent.Task);
    }

    @Subscribe
    public void onEvent(TaskSaveEvent saveEvent) {

        if (saveEvent.task.getId() == null)
            saveEvent.task.setId(UUID.randomUUID().toString());

        if (!challengeTasks.replaceTask(saveEvent.task)) {
            Task taskAbove;

            switch (saveEvent.task.getType()) {
                case Task.TYPE_HABIT:
                    taskAbove = addHabit;
                    break;
                case Task.TYPE_DAILY:
                    taskAbove = addDaily;
                    break;
                case Task.TYPE_TODO:
                    taskAbove = addTodo;
                    break;
                default:
                    taskAbove = addReward;
                    break;
            }

            challengeTasks.addTaskUnder(saveEvent.task, taskAbove);
        }
    }

    private static Task createTask(String taskType, String taskName) {
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

    private class GroupArrayAdapter extends ArrayAdapter<Group> {
        public GroupArrayAdapter(@NonNull Context context) {
            super(context, android.R.layout.simple_spinner_item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppCompatTextView checkedTextView = (AppCompatTextView) super.getView(position, convertView, parent);
            checkedTextView.setText(getItem(position).name);
            return checkedTextView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AppCompatCheckedTextView checkedTextView = (AppCompatCheckedTextView) super.getDropDownView(position, convertView, parent);
            checkedTextView.setText(getItem(position).name);
            return checkedTextView;
        }
    }

    @OnClick(R.id.challenge_add_gem_btn)
    public void onAddGem() {
        int currentVal = Integer.parseInt(create_challenge_prize.getText().toString());
        currentVal++;

        create_challenge_prize.setText("" + currentVal);
    }

    @OnClick(R.id.challenge_remove_gem_btn)
    public void onRemoveGem() {

        int currentVal = Integer.parseInt(create_challenge_prize.getText().toString());
        currentVal--;

        if (currentVal == 0)
            currentVal = 1;

        create_challenge_prize.setText("" + currentVal);
    }
}
