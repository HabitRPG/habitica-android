package com.habitrpg.android.habitica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String type = bundle.getString("type");
        taskId = bundle.getString("taskId");
        if (type == null) {
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
        }

        if (taskType.equals("habit")) {
            LinearLayout actionsLayout = (LinearLayout) findViewById(R.id.task_actions_wrapper);
            mainWrapper.removeView(actionsLayout);
        }

        LinearLayout weekdayWrapper = (LinearLayout)findViewById(R.id.task_weekdays_wrapper);
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
        if (id == R.id.action_settings) {
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


    private void saveTask(Task task) {
        task.text = taskText.getText().toString();
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
                task.getRepeat().setM(this.weekdayCheckboxes.get(0).isChecked());
                task.getRepeat().setT(this.weekdayCheckboxes.get(1).isChecked());
                task.getRepeat().setW(this.weekdayCheckboxes.get(2).isChecked());
                task.getRepeat().setTh(this.weekdayCheckboxes.get(3).isChecked());
                task.getRepeat().setF(this.weekdayCheckboxes.get(4).isChecked());
                task.getRepeat().setS(this.weekdayCheckboxes.get(5).isChecked());
                task.getRepeat().setSu(this.weekdayCheckboxes.get(6).isChecked());
            } else {
                task.setFrequency("daily");
                task.setEveryX(this.frequencyPicker.getValue());
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        this.setDailyFrequencyViews();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        this.setDailyFrequencyViews();
    }

    private void prepareSave() {
        TaskSaveEvent event = new TaskSaveEvent();
        if (this.task == null) {
            event.created = true;
            this.task = new Task();
            this.task.setType(taskType);
        }
        this.saveTask(this.task);

        event.task = this.task;
        EventBus.getDefault().post(event);

    }

    @Override
    public boolean onSupportNavigateUp() {
        this.prepareSave();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        this.prepareSave();
        super.onBackPressed();
    }
}
