package com.habitrpg.android.habitica.ui.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskListWidgetActivity extends BaseActivity  implements TaskTypeSelected {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private int widgetId;

    @Override
    protected int getLayoutResId() {
        return R.layout.widget_configure_task_list;
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

        HashMap<String, String> taskTypesMap = new HashMap<>();
        taskTypesMap.put(getString(R.string.dailies),"dailies");
        taskTypesMap.put(getString(R.string.todos),"todos");

        recyclerView.setAdapter(new TaskTypeSelectionViewAdapter(taskTypesMap, this));
    }

    @Override
    public void taskTypeSelected(String selectedType) {
        finishWithSelection(selectedType);
    }



    public class TaskTypeSelectionViewAdapter extends RecyclerView.Adapter<TaskTypeSelectionViewAdapter.ViewHolder>
    {
        private HashMap<String, String> taskTypes;
        private TaskTypeSelected taskTypeSelected;

        public TaskTypeSelectionViewAdapter(HashMap<String, String> taskTypes, TaskTypeSelected taskTypeSelected) {

            this.taskTypes = taskTypes;
            this.taskTypeSelected = taskTypeSelected;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_configure_task_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(taskTypes.keySet());

            String label = keys.get(position);
            String value = taskTypes.get(label);

            holder.bind(label, value);
        }

        @Override
        public int getItemCount() {
            return taskTypes.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.text)
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setClickable(true);

                ButterKnife.bind(this, itemView);
            }

            private String key;

            public void bind(String label, String key){
                textView.setText(label);
                this.key = key;
            }

            @Override
            public void onClick(View view) {
                taskTypeSelected.taskTypeSelected(key);
            }
        }
    }

    private void finishWithSelection(String selectedTaskType) {
        storeSelectedTaskType(selectedTaskType);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, HabitButtonWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {widgetId});
        sendBroadcast(intent);
    }

    private void storeSelectedTaskType(String selectedTaskType) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
        preferences.putString("habit_list_widget_" + widgetId, selectedTaskType);
        preferences.apply();
    }
}

interface TaskTypeSelected
{
    void taskTypeSelected(String selectedType);
}

