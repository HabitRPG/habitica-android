package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.widget.Button
import androidx.core.content.edit
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.widget.AddTaskWidgetProvider

class AddTaskWidgetActivity : AppCompatActivity() {

    private var widgetId: Int = 0

    private val addHabitButton: Button by bindView(R.id.add_habit_button)
    private val addDailyButton: Button by bindView(R.id.add_daily_button)
    private val addToDoButton: Button by bindView(R.id.add_todo_button)
    private val addRewardButton: Button by bindView(R.id.add_reward_button)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_configure_add_task)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID,
        // finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        addHabitButton.setOnClickListener { addHabitSelected() }
        addDailyButton.setOnClickListener { addDailySelected() }
        addToDoButton.setOnClickListener { addToDoSelected() }
        addRewardButton.setOnClickListener { addRewardSelected() }
    }

    private fun addHabitSelected() {
        finishWithSelection(Task.TYPE_HABIT)
    }

    private fun addDailySelected() {
        finishWithSelection(Task.TYPE_DAILY)
    }

    private fun addToDoSelected() {
        finishWithSelection(Task.TYPE_TODO)
    }

    private fun addRewardSelected() {
        finishWithSelection(Task.TYPE_REWARD)
    }

    private fun finishWithSelection(selectedTaskType: String) {
        storeSelectedTaskType(selectedTaskType)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, AddTaskWidgetProvider::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        sendBroadcast(intent)
    }

    private fun storeSelectedTaskType(selectedTaskType: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putString("add_task_widget_$widgetId", selectedTaskType)
        }
    }
}
