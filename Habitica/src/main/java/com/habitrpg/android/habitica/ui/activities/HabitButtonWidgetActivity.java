package com.habitrpg.android.habitica.ui.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

public class HabitButtonWidgetActivity extends BaseActivity {

    @Inject
    TaskRepository taskRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private int widgetId;
    private SkillTasksRecyclerViewAdapter adapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.widget_configure_habit_button;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(this);

            recyclerView.setLayoutManager(layoutManager);
        }

        adapter = new SkillTasksRecyclerViewAdapter(null, true);
        compositeSubscription.add(adapter.getTaskSelectionEvents().subscribe(task -> taskSelected(task.getId())));
        recyclerView.setAdapter(adapter);

        taskRepository.getTasks(Task.TYPE_HABIT, userId).first().subscribe(adapter::updateData);
    }

    public void taskSelected(String taskId) {
        finishWithSelection(taskId);
    }

    private void finishWithSelection(String selectedTaskId) {
        storeSelectedTaskId(selectedTaskId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, HabitButtonWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        sendBroadcast(intent);
    }

    private void storeSelectedTaskId(String selectedTaskId) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
        preferences.putString("habit_button_widget_" + widgetId, selectedTaskId);
        preferences.apply();
    }
}
