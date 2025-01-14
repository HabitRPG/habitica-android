package com.habitrpg.wearos.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityMainBinding
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.models.user.MenuItem
import com.habitrpg.wearos.habitica.ui.adapters.HubAdapter
import com.habitrpg.wearos.habitica.ui.viewmodels.MainViewModel
import com.habitrpg.wearos.habitica.util.HabiticaScrollingLayoutCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override val viewModel: MainViewModel by viewModels()
    private val adapter =
        HubAdapter().apply {
            onRefresh = {
                viewModel.retrieveFullUserData()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.recyclerView.apply {
            layoutManager =
                WearableLinearLayoutManager(this@MainActivity, HabiticaScrollingLayoutCallback())
            adapter = this@MainActivity.adapter
        }
        lifecycleScope.launch {
            appStateManager.isAppConnected.collect {
                adapter.isDisconnected = !it
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.data =
            listOf(
                MenuItem(
                    "createTask",
                    getString(R.string.new_task),
                    AppCompatResources.getDrawable(this, R.drawable.icon_plus),
                    ContextCompat.getColor(this, R.color.watch_purple_100),
                    ContextCompat.getColor(this, R.color.black),
                    true
                ) {
                    openTaskFormActivity()
                },
                MenuItem(
                    TaskType.DAILY.value,
                    getString(R.string.dailies),
                    AppCompatResources.getDrawable(this, R.drawable.icon_dailies),
                    ContextCompat.getColor(this, R.color.watch_purple_200),
                    ContextCompat.getColor(this, R.color.watch_purple_700)
                ) {
                    openTasklist(TaskType.DAILY)
                },
                MenuItem(
                    TaskType.TODO.value,
                    getString(R.string.todos),
                    AppCompatResources.getDrawable(this, R.drawable.icon_todos),
                    ContextCompat.getColor(this, R.color.watch_purple_200),
                    ContextCompat.getColor(this, R.color.watch_purple_700)
                ) {
                    openTasklist(TaskType.TODO)
                },
                MenuItem(
                    TaskType.HABIT.value,
                    getString(R.string.habits),
                    AppCompatResources.getDrawable(this, R.drawable.icon_habits),
                    ContextCompat.getColor(this, R.color.watch_purple_200),
                    ContextCompat.getColor(this, R.color.watch_purple_700)
                ) {
                    openTasklist(TaskType.HABIT)
                },
                MenuItem(
                    "stats",
                    getString(R.string.stats),
                    AppCompatResources.getDrawable(this, R.drawable.icon_stats),
                    ContextCompat.getColor(this, R.color.watch_purple_200),
                    ContextCompat.getColor(this, R.color.watch_purple_700)
                ) {
                    openStatsActivity()
                },
                MenuItem(
                    "avatar",
                    getString(R.string.avatar),
                    AppCompatResources.getDrawable(this, R.drawable.icon_avatar),
                    ContextCompat.getColor(this, R.color.watch_purple_200),
                    ContextCompat.getColor(this, R.color.watch_purple_700)
                ) {
                    openAvatarActivity()
                },
                MenuItem(
                    "settings",
                    getString(R.string.settings),
                    AppCompatResources.getDrawable(this, R.drawable.icon_settings),
                    ContextCompat.getColor(this, R.color.watch_purple_200),
                    ContextCompat.getColor(this, R.color.watch_purple_700)
                ) {
                    openSettingsActivity()
                }
            )
        lifecycleScope.launch {
            viewModel.user
                .filterNotNull()
                .collect { user ->
                    adapter.title = user.profile?.name ?: ""
                    val index = adapter.data.indexOfFirst { it.identifier == "stats" }
                    adapter.data[index].detailText = getString(R.string.user_level, user.stats?.lvl ?: 0)
                    adapter.notifyItemChanged(index + 1)
                }
        }

        viewModel.taskCounts.observe(this) {
            adapter.data.forEach { menuItem ->
                if (it.containsKey(menuItem.identifier)) {
                    if (it[menuItem.identifier]!! > 0) {
                        menuItem.detailText = it[menuItem.identifier].toString()
                    } else {
                        menuItem.detailText = null
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.periodicUserRefresh()
    }

    private val openTaskForm =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val taskType = result.data?.getStringExtra("task_type")?.let { TaskType.from(it) }
                if (taskType != null) {
                    openTasklist(taskType)
                }
            }
        }

    private fun openTaskFormActivity() {
        openTaskForm.launch(Intent(this, TaskFormActivity::class.java))
    }

    private fun openAvatarActivity() {
        startActivity(Intent(this, AvatarActivity::class.java))
    }

    private fun openStatsActivity() {
        startActivity(Intent(this, StatsActivity::class.java))
    }

    private fun openSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openTasklist(type: TaskType) {
        val intent =
            Intent(this, TaskListActivity::class.java).apply {
                putExtra("task_type", type.value)
            }
        startActivity(intent)
    }
}
