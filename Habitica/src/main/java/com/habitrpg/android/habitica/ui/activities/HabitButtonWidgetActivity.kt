package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

class HabitButtonWidgetActivity : BaseActivity() {

    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    internal val recyclerView: androidx.recyclerview.widget.RecyclerView by bindView(R.id.recyclerView)
    private var widgetId: Int = 0
    private var adapter: SkillTasksRecyclerViewAdapter? = null

    override fun getLayoutResId(): Int {
        return R.layout.widget_configure_habit_button
    }

    override fun injectActivity(component: AppComponent?) {
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

        var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager

        if (layoutManager == null) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

            recyclerView.layoutManager = layoutManager
        }

        adapter = SkillTasksRecyclerViewAdapter(null, true)
        adapter?.getTaskSelectionEvents()?.subscribe(Consumer { task -> taskSelected(task.id) },
                RxErrorHandler.handleEmptyError())
                .notNull { compositeSubscription.add(it) }
        recyclerView.adapter = adapter

        taskRepository.getTasks(Task.TYPE_HABIT, userId).firstElement().subscribe(Consumer { adapter?.updateData(it) }, RxErrorHandler.handleEmptyError())
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
        val preferences = PreferenceManager.getDefaultSharedPreferences(this).edit()
        preferences.putString("habit_button_widget_$widgetId", selectedTaskId)
        preferences.apply()
    }
}
