package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.ActivityMainBinding
import com.habitrpg.wearos.habitica.models.MenuItem
import com.habitrpg.wearos.habitica.ui.adapters.HubAdapter
import com.habitrpg.wearos.habitica.ui.viewmodels.MainViewModel
import com.habitrpg.wearos.habitica.util.HabiticaScrollingLayoutCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override val viewModel: MainViewModel by viewModels()

    private val adapter = HubAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.root.apply {
            layoutManager =
                WearableLinearLayoutManager(this@MainActivity, HabiticaScrollingLayoutCallback())
            adapter = this@MainActivity.adapter
        }
        binding.root.post {
            binding.root.setPaddingRelative(0, (binding.root.height * 0.25).toInt(), 0, (binding.root.height * 0.25).toInt())
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.data = listOf(
            MenuItem(
                "createTask",
                getString(R.string.new_task),
                AppCompatResources.getDrawable(this, R.drawable.icon_plus),
                ContextCompat.getColor(this, R.color.brand_400),
                true
            ) {
                openTaskFormActivity()
            },
            MenuItem(
                "habits",
                getString(R.string.habits),
                AppCompatResources.getDrawable(this, R.drawable.icon_habits),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
              openTasklist(TaskType.HABIT)
            },
            MenuItem(
                "dailies",
                getString(R.string.dailies),
                AppCompatResources.getDrawable(this, R.drawable.icon_dailies),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
                openTasklist(TaskType.DAILY)
            },
            MenuItem(
                "todos",
                getString(R.string.todos),
                AppCompatResources.getDrawable(this, R.drawable.icon_todos),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
                openTasklist(TaskType.TODO)
            },
            MenuItem(
                "rewards",
                getString(R.string.rewards),
                AppCompatResources.getDrawable(this, R.drawable.icon_rewards),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
                openTasklist(TaskType.REWARD)
            },
            MenuItem(
                "Stats",
                getString(R.string.stats),
                AppCompatResources.getDrawable(this, R.drawable.icon_stats),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
                openStatsActivity()
            },
            MenuItem(
                "avatar",
                getString(R.string.avatar),
                AppCompatResources.getDrawable(this, R.drawable.icon_avatar),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
                openAvatarActivity()
            },
            MenuItem(
                "settings",
                getString(R.string.settings),
                AppCompatResources.getDrawable(this, R.drawable.icon_settings),
                ContextCompat.getColor(this, R.color.brand_400)
            ) {
                openSettingsActivity()
            }
        )
        viewModel.user.observe(this) {
            adapter.title = it.profile?.name ?: ""
            adapter.notifyItemChanged(0)
        }
    }

    private fun openTaskFormActivity() {
        startActivity(Intent(this, TaskFormActivity::class.java))
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
        val intent = Intent(this, TaskListActivity::class.java).apply {
            putExtra("task_type", type.value)
        }
        startActivity(intent)
    }
}