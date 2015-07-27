package com.habitrpg.android.habitica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;
import com.raizlabs.android.dbflow.sql.language.Select;

import static com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType.daily;
import static com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType.habit;
import static com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType.reward;
import static com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType.todo;

public class TaskFormActivity extends AppCompatActivity {

    private HabitType taskType;
    private String taskId;

    private EditText taskText, taskNotes;
    private Spinner taskDifficulty;
    private CheckBox positiveCheckBox, negativeCheckBox;

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
        taskType = type.equals(daily.toString()) ? daily
                : type.equals(reward.toString()) ? reward
                : type.equals(todo.toString()) ? todo
                : habit;

        LinearLayout mainWrapper = (LinearLayout) findViewById(R.id.task_main_wrapper);
        taskText = (EditText) findViewById(R.id.task_text_edittext);
        taskNotes = (EditText) findViewById(R.id.task_notes_edittext);
        taskDifficulty = (Spinner) findViewById(R.id.task_difficulty_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_difficulties, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskDifficulty.setAdapter(adapter);

        if (taskType == habit) {
            LinearLayout startDateLayout = (LinearLayout) findViewById(R.id.task_startdate_layout);
            LinearLayout taskWrapper = (LinearLayout) findViewById(R.id.task_task_wrapper);
            taskWrapper.removeView(startDateLayout);

            LinearLayout checklistLayout = (LinearLayout) findViewById(R.id.task_checklist_wrapper);
            mainWrapper.removeView(checklistLayout);

            positiveCheckBox = (CheckBox) findViewById(R.id.task_positive_checkbox);
            negativeCheckBox = (CheckBox) findViewById(R.id.task_negative_checkbox);
        }

        if (taskType != habit) {
            LinearLayout actionsLayout = (LinearLayout) findViewById(R.id.task_actions_wrapper);
            mainWrapper.removeView(actionsLayout);
        }

        if (taskId != null) {
            switch (taskType) {
                case todo:
                    ToDo todo = new Select().from(ToDo.class).byIds(taskId).querySingle();
                    this.populate(todo);
                    break;
                case daily:
                    Daily daily = new Select().from(Daily.class).byIds(taskId).querySingle();
                    this.populate(daily);
                    break;
                case habit:
                    Habit habit = new Select().from(Habit.class).byIds(taskId).querySingle();
                    this.populate(habit);
                    break;
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


    private void populate(HabitItem task) {
        taskText.setText(task.text);
        taskNotes.setText(task.notes);
    }

    private void populate(Habit task) {
        populate((HabitItem) task);
        positiveCheckBox.setChecked(task.getUp());
        negativeCheckBox.setChecked(task.getDown());
    }

    private void populate(Daily task) {
        populate((HabitItem) task);
    }

    private void populate(ToDo task) {
        populate((HabitItem) task);
    }
}
