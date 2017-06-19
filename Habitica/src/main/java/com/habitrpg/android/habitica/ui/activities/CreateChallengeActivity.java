package com.habitrpg.android.habitica.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.underscore.$;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;

public class CreateChallengeActivity extends BaseActivity {
    public static final String CHALLENGE_ID_KEY = "challengeId";

    @BindView(R.id.create_challenge_title_input_layout)
    TextInputLayout createChallengeTitleInputLayout;

    @BindView(R.id.create_challenge_title)
    EditText createChallengeTitle;

    @BindView(R.id.create_challenge_description_input_layout)
    TextInputLayout createChallengeDescriptionInputLayout;

    @BindView(R.id.create_challenge_description)
    EditText createChallengeDescription;

    @BindView(R.id.create_challenge_prize)
    EditText createChallengePrize;


    @BindView(R.id.create_challenge_tag_input_layout)
    TextInputLayout createChallengeTagInputLayout;

    @BindView(R.id.create_challenge_tag)
    EditText createChallengeTag;

    @BindView(R.id.create_challenge_gem_error)
    TextView createChallengeGemError;

    @BindView(R.id.create_challenge_task_error)
    TextView createChallengeTaskError;

    @BindView(R.id.challenge_location_spinner)
    Spinner challengeLocationSpinner;

    @BindView(R.id.challenge_add_gem_btn)
    Button challengeAddGemBtn;

    @BindView(R.id.challenge_remove_gem_btn)
    Button challengeRemoveGemBtn;

    @BindView(R.id.create_challenge_task_list)
    RecyclerView createChallengeTaskList;

    @Inject
    ChallengeRepository challengeRepository;
    @Inject
    SocialRepository socialRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    private ChallengeTasksRecyclerViewAdapter challengeTasks;

    private GroupArrayAdapter locationAdapter;
    private String challengeId;
    private boolean editMode;

    private HashMap<String, Task> addedTasks = new HashMap<>();
    private HashMap<String, Task> updatedTasks = new HashMap<>();
    private HashMap<String, Task> removedTasks = new HashMap<>();

    // Add {*} Items
    Task addHabit;
    Task addDaily;
    Task addTodo;
    Task addReward;
    @Nullable
    private User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        userRepository.getUser(userId).subscribe(user1 -> this.user = user1, RxErrorHandler.handleEmptyError());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_create_challenge;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_challenge, menu);
        return true;
    }

    private boolean savingInProgress = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save && !savingInProgress && validateAllFields()) {
            savingInProgress = true;
            ProgressDialog dialog = ProgressDialog.show(this, "", "Saving challenge data. Please wait...", true, false);

            Observable<Challenge> observable;

            if (editMode) {
                observable = updateChallenge();
            } else {
                observable = createChallenge();
            }

            observable.subscribe(challenge -> {
                dialog.dismiss();
                savingInProgress = false;
                finish();
            }, throwable ->  {
                dialog.dismiss();
                savingInProgress = false;
            });
        } else if(item.getItemId() == android.R.id.home){
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validateAllFields() {
        ArrayList<String> errorMessages = new ArrayList<>();

        if (getEditTextString(createChallengeTitle).isEmpty()) {
            String titleEmptyError = getString(R.string.challenge_create_error_title);
            createChallengeTitleInputLayout.setError(titleEmptyError);
            errorMessages.add(titleEmptyError);
        } else {
            createChallengeTitleInputLayout.setErrorEnabled(false);
        }

        if (getEditTextString(createChallengeTag).isEmpty()) {
            String tagEmptyError = getString(R.string.challenge_create_error_tag);

            createChallengeTagInputLayout.setError(tagEmptyError);
            errorMessages.add(tagEmptyError);
        } else {
            createChallengeTagInputLayout.setErrorEnabled(false);
        }

        String prizeError = checkPrizeAndMinimumForTavern();

        if(!prizeError.isEmpty()){
            errorMessages.add(prizeError);
        }

        // all "Add {*}"-Buttons are one task itself, so we need atleast more than 4
        if (challengeTasks.getTaskList().size() <= 4) {
            createChallengeTaskError.setVisibility(View.VISIBLE);
            errorMessages.add(getString(R.string.challenge_create_error_no_tasks));
        } else {
            createChallengeTaskError.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage($.join(errorMessages, "\n"));

        AlertDialog alert = builder.create();
        alert.show();

        return errorMessages.size() == 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            challengeId = bundle.getString(CHALLENGE_ID_KEY, null);
        }

        EventBus.getDefault().register(this);

        fillControls();

        if (challengeId != null) {
            fillControlsByChallenge();
        }
    }


    @Override
    public void onDestroy() {
        socialRepository.close();
        challengeRepository.close();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(TaskTappedEvent tappedEvent) {
        openNewTaskActivity(null, tappedEvent.Task);
    }

    @Subscribe
    public void onEvent(TaskSaveEvent saveEvent) {

        if (saveEvent.task.getId() == null) {
            saveEvent.task.setId(UUID.randomUUID().toString());
        }

        addOrUpdateTaskInList(saveEvent.task);
    }

    @OnClick(R.id.challenge_add_gem_btn)
    public void onAddGem() {
        int currentVal = Integer.parseInt(createChallengePrize.getText().toString());
        currentVal++;

        createChallengePrize.setText("" + currentVal);

        checkPrizeAndMinimumForTavern();
    }

    @OnClick(R.id.challenge_remove_gem_btn)
    public void onRemoveGem() {

        int currentVal = Integer.parseInt(createChallengePrize.getText().toString());
        currentVal--;

        createChallengePrize.setText("" + currentVal);

        checkPrizeAndMinimumForTavern();
    }

    private String checkPrizeAndMinimumForTavern() {
        String errorResult = "";

        String inputValue = createChallengePrize.getText().toString();

        if (inputValue.isEmpty()) {
            inputValue = "0";
        }

        int currentVal = Integer.parseInt(inputValue);

        // 0 is Tavern
        int selectedLocation = challengeLocationSpinner.getSelectedItemPosition();

        double gemCount = 0;
        if (user != null) {
            gemCount = user.getGemCount();
        }

        if (selectedLocation == 0 && currentVal == 0) {
            createChallengeGemError.setVisibility(View.VISIBLE);
            String error = getString(R.string.challenge_create_error_tavern_one_gem);
            createChallengeGemError.setText(error);
            errorResult = error;
        } else if (currentVal > gemCount) {
            createChallengeGemError.setVisibility(View.VISIBLE);
            String error = getString(R.string.challenge_create_error_enough_gems);
            createChallengeGemError.setText(error);
            errorResult = error;
        } else {
            createChallengeGemError.setVisibility(View.GONE);
        }

        challengeRemoveGemBtn.setEnabled(currentVal != 0);

        return errorResult;
    }

    private void fillControls() {
        Resources resources = getResources();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

            supportActionBar.setTitle("");
            supportActionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.brand_200)));
            supportActionBar.setElevation(0);
        }

        locationAdapter = new GroupArrayAdapter(this);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialRepository.getGroups("guild").subscribe(groups -> {
            Group tavern = new Group();
            tavern.id = "00000000-0000-4000-A000-000000000000";
            tavern.name = getString(R.string.sidebar_tavern);

            locationAdapter.add(tavern);

            for (Group group : groups) {
                locationAdapter.add(group);
            }
        }, RxErrorHandler.handleEmptyError());

        challengeLocationSpinner.setAdapter(locationAdapter);
        challengeLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                checkPrizeAndMinimumForTavern();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        createChallengePrize.setOnKeyListener((view, i, keyEvent) -> {
            checkPrizeAndMinimumForTavern();

            return false;
        });

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
        challengeTasks.addItemObservable().subscribe(t -> {
            if (t.equals(addHabit)) {
                openNewTaskActivity(Task.TYPE_HABIT, null);
            } else if (t.equals(addDaily)) {
                openNewTaskActivity(Task.TYPE_DAILY, null);
            } else if (t.equals(addTodo)) {
                openNewTaskActivity(Task.TYPE_TODO, null);
            } else if (t.equals(addReward)) {
                openNewTaskActivity(Task.TYPE_REWARD, null);
            }
        });

        createChallengeTaskList.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                // Stop only scrolling.
                return rv.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;
            }
        });
        createChallengeTaskList.setAdapter(challengeTasks);
        createChallengeTaskList.setLayoutManager(new LinearLayoutManager(this));
    }

    private static Task createTask(String taskType, String taskName) {
        Task t = new Task();

        t.setId(UUID.randomUUID().toString());
        t.setType(taskType);
        t.setText(taskName);

        if (taskType.equals(Task.TYPE_HABIT)) {
            t.setUp(true);
            t.setDown(false);
        }

        return t;
    }

    private void fillControlsByChallenge() {
        challengeRepository.getChallenge(challengeId).subscribe(challenge -> {

            createChallengeTitle.setText(challenge.name);
            createChallengeDescription.setText(challenge.description);
            createChallengeTag.setText(challenge.shortName);
            createChallengePrize.setText(String.valueOf(challenge.prize));

            for (int i = 0; i < locationAdapter.getCount(); i++) {
                Group group = locationAdapter.getItem(i);

                if (group != null && challenge.groupId.equals(group.id)) {
                    challengeLocationSpinner.setSelection(i);
                    break;
                }
            }

            checkPrizeAndMinimumForTavern();

            challengeRepository.getChallengeTasks(challengeId).subscribe(tasks -> {
                tasks.tasks.forEach((s, task) -> addOrUpdateTaskInList(task));
            }, Throwable::printStackTrace, () -> {
                // activate editMode to track taskChanges
                editMode = true;
            });
        });
    }

    private void openNewTaskActivity(String type, Task task) {
        Bundle bundle = new Bundle();

        if (task == null) {
            bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type);
        } else {
            bundle.putParcelable(TaskFormActivity.PARCELABLE_TASK, task);
        }

        bundle.putBoolean(TaskFormActivity.SAVE_TO_DB, false);
        bundle.putBoolean(TaskFormActivity.SET_IGNORE_FLAG, true);
        bundle.putBoolean(TaskFormActivity.SHOW_TAG_SELECTION, false);
        bundle.putBoolean(TaskFormActivity.SHOW_CHECKLIST, false);

        if (user != null && user.getPreferences() != null) {
            String allocationMode = user.getPreferences().getAllocationMode();

            bundle.putString(TaskFormActivity.USER_ID_KEY, user.getId());
            bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);
        }

        Intent intent = new Intent(this, TaskFormActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, 1);
    }

    private Challenge getChallengeData() {
        Challenge c = new Challenge();

        int locationPos = challengeLocationSpinner.getSelectedItemPosition();
        Group locationGroup = locationAdapter.getItem(locationPos);

        if (challengeId != null) {
            c.id = challengeId;
        }

        c.groupId = locationGroup.id;
        c.name = createChallengeTitle.getText().toString();
        c.description = createChallengeDescription.getText().toString();
        c.shortName = createChallengeTag.getText().toString();
        c.prize = Integer.parseInt(createChallengePrize.getText().toString());

        return c;
    }

    private Observable<Challenge> createChallenge() {
        Challenge c = getChallengeData();

        List<Task> taskList = challengeTasks.getTaskList();
        taskList.remove(addHabit);
        taskList.remove(addDaily);
        taskList.remove(addTodo);
        taskList.remove(addReward);

        return challengeRepository.createChallenge(c, taskList);
    }

    private Observable<Challenge> updateChallenge() {
        Challenge c = getChallengeData();

        List<Task> taskList = challengeTasks.getTaskList();
        taskList.remove(addHabit);
        taskList.remove(addDaily);
        taskList.remove(addTodo);
        taskList.remove(addReward);

        return challengeRepository.updateChallenge(c, taskList, new ArrayList<>(addedTasks.values()),
                new ArrayList<>(updatedTasks.values()),
                new ArrayList<>(removedTasks.keySet())
        );
    }

    private void addOrUpdateTaskInList(Task task) {
        if (!challengeTasks.replaceTask(task)) {
            Task taskAbove;

            switch (task.getType()) {
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

            challengeTasks.addTaskUnder(task, taskAbove);

            if (editMode) {
                addedTasks.put(task.getId(), task);
            }
        } else {
            // don't need to add the task to updatedTasks if its already been added right now
            if (editMode && !addedTasks.containsKey(task.getId())) {
                updatedTasks.put(task.getId(), task);
            }
        }
    }

    private String getEditTextString(EditText editText) {
        return editText.getText().toString();
    }

    private class GroupArrayAdapter extends ArrayAdapter<Group> {
        GroupArrayAdapter(@NonNull Context context) {
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
}
