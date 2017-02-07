package com.habitrpg.android.habitica.ui.activities;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.widget.AddTaskWidgetProvider;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddTaskWidgetActivity extends AppCompatActivity {

    private int widgetId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_configure_add_task);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID,
        // finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    @OnClick(R.id.add_habit_button)
    public void addHabitSelected() {
        finishWithSelection(Task.TYPE_HABIT);
    }

    @OnClick(R.id.add_daily_button)
    public void addDailySelected() {
        finishWithSelection(Task.TYPE_DAILY);
    }

    @OnClick(R.id.add_todo_button)
    public void addToDoSelected() {
        finishWithSelection(Task.TYPE_TODO);
    }

    @OnClick(R.id.add_reward_button)
    public void addRewardSelected() {
        finishWithSelection(Task.TYPE_REWARD);
    }

    private void finishWithSelection(String selectedTaskType) {
        storeSelectedTaskType(selectedTaskType);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, AddTaskWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        sendBroadcast(intent);
    }

    private void storeSelectedTaskType(String selectedTaskType) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
        preferences.putString("add_task_widget_" + widgetId, selectedTaskType);
        preferences.apply();
    }
}
