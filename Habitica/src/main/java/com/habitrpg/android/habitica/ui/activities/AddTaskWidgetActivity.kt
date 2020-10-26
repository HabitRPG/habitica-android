package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.databinding.WidgetConfigureAddTaskBinding
import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.android.habitica.widget.AddTaskWidgetProvider
import com.habitrpg.shared.habitica.models.tasks.TaskType

class AddTaskWidgetActivity : AppCompatActivity() {

    private var widgetId: Int = 0

    private lateinit var binding: WidgetConfigureAddTaskBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        binding = WidgetConfigureAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.addHabitButton.setOnClickListener { addHabitSelected() }
        binding.addDailyButton.setOnClickListener { addDailySelected() }
        binding.addTodoButton.setOnClickListener { addToDoSelected() }
        binding.addRewardButton.setOnClickListener { addRewardSelected() }
    }

    private fun addHabitSelected() {
        finishWithSelection(TaskType.TYPE_HABIT)
    }

    private fun addDailySelected() {
        finishWithSelection(TaskType.TYPE_DAILY)
    }

    private fun addToDoSelected() {
        finishWithSelection(TaskType.TYPE_TODO)
    }

    private fun addRewardSelected() {
        finishWithSelection(TaskType.TYPE_REWARD)
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
