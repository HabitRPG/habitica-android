package com.habitrpg.android.habitica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Days;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class TaskFormActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String taskType;
    private String taskId;
    private Task task;

    private EditText taskText, taskNotes;
    private Spinner taskDifficultySpinner, dailyFrequencySpinner;
    private CheckBox positiveCheckBox, negativeCheckBox;

    private List<CheckBox> weekdayCheckboxes = new ArrayList<CheckBox>();
    private NumberPicker frequencyPicker;
    private LinearLayout frequencyContainer;
    private List<String> tags;
    private TransactionListener<List<Tag>> tagsSearchingListener = new TransactionListener<List<Tag>>() {
        @Override
        public void onResultReceived(List<Tag> tags) {
            //UI thread.
            List<TaskTag> taskTags = new ArrayList<TaskTag>();
            for (Tag tag : tags) {
                TaskTag tt = new TaskTag();
                tt.setTag(tag);
                tt.setTask(task);
                taskTags.add(tt);
            }
            //save
            TaskFormActivity.this.task.setTags(taskTags);
            TaskFormActivity.this.task.update();
            //send back to other elements.
            TaskSaveEvent event = new TaskSaveEvent();
            if (TaskFormActivity.this.task.getId() == null) {
                event.created = true;
            }

            event.task = TaskFormActivity.this.task;
            EventBus.getDefault().post(event);

        }

        @Override
        public boolean onReady(BaseTransaction<List<Tag>> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<List<Tag>> baseTransaction, List<Tag> tags) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        taskType = bundle.getString("type");
        taskId = bundle.getString("taskId");
        tags = bundle.getStringArrayList("tagsId");
        if (taskType == null) {
            return;
        }

        LinearLayout mainWrapper = (LinearLayout) findViewById(R.id.task_main_wrapper);
        taskText = (EditText) findViewById(R.id.task_text_edittext);
        taskNotes = (EditText) findViewById(R.id.task_notes_edittext);
        taskDifficultySpinner = (Spinner) findViewById(R.id.task_difficulty_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_difficulties, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskDifficultySpinner.setAdapter(adapter);

        if (taskType.equals("habit")) {
            LinearLayout startDateLayout = (LinearLayout) findViewById(R.id.task_startdate_layout);
            LinearLayout taskWrapper = (LinearLayout) findViewById(R.id.task_task_wrapper);
            taskWrapper.removeView(startDateLayout);

            LinearLayout checklistLayout = (LinearLayout) findViewById(R.id.task_checklist_wrapper);
            mainWrapper.removeView(checklistLayout);

            positiveCheckBox = (CheckBox) findViewById(R.id.task_positive_checkbox);
            negativeCheckBox = (CheckBox) findViewById(R.id.task_negative_checkbox);
        } else {
            LinearLayout actionsLayout = (LinearLayout) findViewById(R.id.task_actions_wrapper);
            mainWrapper.removeView(actionsLayout);
        }

        LinearLayout weekdayWrapper = (LinearLayout) findViewById(R.id.task_weekdays_wrapper);
        if (taskType.equals("daily")) {
            this.dailyFrequencySpinner = (Spinner) weekdayWrapper.findViewById(R.id.task_frequency_spinner);

            ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this,
                    R.array.daily_frequencies, android.R.layout.simple_spinner_item);
            frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.dailyFrequencySpinner.setAdapter(frequencyAdapter);
            this.dailyFrequencySpinner.setOnItemSelectedListener(this);

            this.frequencyContainer = (LinearLayout) weekdayWrapper.findViewById(R.id.task_frequency_container);
        } else {
            mainWrapper.removeView(weekdayWrapper);
        }


        if (taskId != null) {
            Task task = new Select().from(Task.class).byIds(taskId).querySingle();
            this.task = task;
            this.populate(task);
            setTitle(task);
        } else {
            setTitle((Task) null);
        }
    }

    private void setTitle(Task task) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            String title = "";

            if (task != null) {
                title = getResources().getString(R.string.action_edit) + " " + task.getText();
            } else {
                switch (taskType) {
                    case "todo":
                        title = getResources().getString(R.string.new_todo);
                        break;
                    case "daily":
                        title = getResources().getString(R.string.new_daily);
                        break;
                    case "habit":
                        title = getResources().getString(R.string.new_habit);
                        break;
                    case "reward":
                        title = getResources().getString(R.string.new_reward);
                        break;
                }
            }

            actionBar.setTitle(title);
        }
    }

    private void setDailyFrequencyViews() {
        this.frequencyContainer.removeAllViews();
        if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
            String[] weekdays = getResources().getStringArray(R.array.weekdays);
            for (int i = 0; i < 7; i++) {
                View weekdayRow = getLayoutInflater().inflate(R.layout.row_checklist, null);
                TextView tv = (TextView) weekdayRow.findViewById(R.id.label);
                CheckBox checkbox = (CheckBox) weekdayRow.findViewById(R.id.checkbox);
                checkbox.setChecked(true);
                this.weekdayCheckboxes.add(checkbox);
                tv.setText(weekdays[i]);
                this.frequencyContainer.addView(weekdayRow);
            }
        } else {
            View dayRow = getLayoutInflater().inflate(R.layout.row_number_picker, null);
            this.frequencyPicker = (NumberPicker) dayRow.findViewById(R.id.numberPicker);
            this.frequencyPicker.setMinValue(1);
            this.frequencyPicker.setMaxValue(366);
            TextView tv = (TextView) dayRow.findViewById(R.id.label);
            tv.setText(getResources().getString(R.string.frequency_daily));
            this.frequencyContainer.addView(dayRow);
        }

        if (this.task != null) {

            if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
                this.weekdayCheckboxes.get(0).setChecked(this.task.getRepeat().getM());
                this.weekdayCheckboxes.get(1).setChecked(this.task.getRepeat().getT());
                this.weekdayCheckboxes.get(2).setChecked(this.task.getRepeat().getW());
                this.weekdayCheckboxes.get(3).setChecked(this.task.getRepeat().getTh());
                this.weekdayCheckboxes.get(4).setChecked(this.task.getRepeat().getF());
                this.weekdayCheckboxes.get(5).setChecked(this.task.getRepeat().getS());
                this.weekdayCheckboxes.get(6).setChecked(this.task.getRepeat().getSu());
            } else {
                this.frequencyPicker.setValue(this.task.getEveryX());
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_discard_changes) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populate(Task task) {
        taskText.setText(task.text);
        taskNotes.setText(task.notes);
        float priority = task.getPriority();
        if (priority == 0.1) {
            this.taskDifficultySpinner.setSelection(0);
        } else if (priority == 1.0) {
            this.taskDifficultySpinner.setSelection(1);
        } else if (priority == 1.5) {
            this.taskDifficultySpinner.setSelection(2);
        } else if (priority == 2.0) {
            this.taskDifficultySpinner.setSelection(3);
        }

        if (task.type.equals("habit")) {
            positiveCheckBox.setChecked(task.getUp());
            negativeCheckBox.setChecked(task.getDown());
        }

        if (task.type.equals("daily")) {
            if (task.getFrequency().equals("weekly")) {
                this.dailyFrequencySpinner.setSelection(0);
                if (weekdayCheckboxes.size() == 7) {
                    this.weekdayCheckboxes.get(0).setChecked(task.getRepeat().getM());
                    this.weekdayCheckboxes.get(1).setChecked(task.getRepeat().getT());
                    this.weekdayCheckboxes.get(2).setChecked(task.getRepeat().getW());
                    this.weekdayCheckboxes.get(3).setChecked(task.getRepeat().getTh());
                    this.weekdayCheckboxes.get(4).setChecked(task.getRepeat().getF());
                    this.weekdayCheckboxes.get(5).setChecked(task.getRepeat().getS());
                    this.weekdayCheckboxes.get(6).setChecked(task.getRepeat().getSu());
                }
            } else {
                this.dailyFrequencySpinner.setSelection(1);
                if (this.frequencyPicker != null) {
                    this.frequencyPicker.setValue(task.getEveryX());
                }
            }
        }

    }

    private boolean saveTask(Task task) {
        task.text = taskText.getText().toString();

        if (task.text.isEmpty())
            return false;

        task.notes = taskNotes.getText().toString();

        if (this.taskDifficultySpinner.getSelectedItemPosition() == 0) {
            task.setPriority((float) 0.1);
        } else if (this.taskDifficultySpinner.getSelectedItemPosition() == 1) {
            task.setPriority((float) 1.0);
        } else if (this.taskDifficultySpinner.getSelectedItemPosition() == 2) {
            task.setPriority((float) 1.5);
        } else if (this.taskDifficultySpinner.getSelectedItemPosition() == 3) {
            task.setPriority((float) 2.0);
        }

        if (task.type.equals("habit")) {
            task.setUp(positiveCheckBox.isChecked());
            task.setDown(negativeCheckBox.isChecked());
        }

        if (task.type.equals("daily")) {
            if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
                task.setFrequency("weekly");
                Days repeat = task.getRepeat();
                if (repeat == null) {
                    repeat = new Days();
                    task.setRepeat(repeat);
                }

                repeat.setM(this.weekdayCheckboxes.get(0).isChecked());
                repeat.setT(this.weekdayCheckboxes.get(1).isChecked());
                repeat.setW(this.weekdayCheckboxes.get(2).isChecked());
                repeat.setTh(this.weekdayCheckboxes.get(3).isChecked());
                repeat.setF(this.weekdayCheckboxes.get(4).isChecked());
                repeat.setS(this.weekdayCheckboxes.get(5).isChecked());
                repeat.setSu(this.weekdayCheckboxes.get(6).isChecked());
            } else {
                task.setFrequency("daily");
                task.setEveryX(this.frequencyPicker.getValue());
            }
        }

        return true;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        this.setDailyFrequencyViews();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        this.setDailyFrequencyViews();
    }

    private void prepareSave() {
        if (this.task == null) {
            this.task = new Task();
            this.task.setType(taskType);
        }
        if (this.saveTask(this.task)) {
            this.task.save();
            List<TaskTag> taskTags = new ArrayList<TaskTag>();
            new Select()
                    .from(Tag.class)
                    .where(Condition.column("id").in("", tags.toArray())).async().queryList(tagsSearchingListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.prepareSave();
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        this.prepareSave();
        finish();
    }
}
