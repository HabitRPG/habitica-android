package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.WidgetConfigureHabitButtonBinding
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class HabitButtonWidgetActivity : BaseActivity() {
    private val job = SupervisorJob()

    private lateinit var binding: WidgetConfigureHabitButtonBinding

    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private var widgetId: Int = 0
    private var adapter: SkillTasksRecyclerViewAdapter? = null

    override fun getLayoutResId(): Int {
        return R.layout.widget_configure_habit_button
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = WidgetConfigureHabitButtonBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        var layoutManager: LinearLayoutManager? = binding.recyclerView.layoutManager as? LinearLayoutManager

        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(this)

            binding.recyclerView.layoutManager = layoutManager
        }

        adapter = SkillTasksRecyclerViewAdapter()
        adapter?.onTaskSelection = {
            taskSelected(it.id)
        }
        binding.recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.Main + job).launch(ExceptionHandler.coroutine()) {
            adapter?.data = taskRepository.getTasks(TaskType.HABIT, userId, emptyArray()).firstOrNull() ?: listOf()
        }
    }

    private fun taskSelected(taskId: String?) {
        finishWithSelection(taskId)
    }

    private fun finishWithSelection(selectedTaskId: String?) {
        storeSelectedTaskId(selectedTaskId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, HabitButtonWidgetProvider::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        sendBroadcast(intent)
    }

    private fun storeSelectedTaskId(selectedTaskId: String?) {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putString("habit_button_widget_$widgetId", selectedTaskId)
        }
    }
}
