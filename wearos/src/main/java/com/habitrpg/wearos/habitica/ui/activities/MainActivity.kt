package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.ActivityMainBinding
import com.habitrpg.wearos.habitica.ui.adapters.HubAdapter
import com.habitrpg.wearos.habitica.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

data class MenuItem(
    val identifier: String,
    val title: String,
    val icon: Drawable?,
    val onClick: () -> Unit
)

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    val viewModel: MainViewModel by viewModels()

    private val adapter = HubAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.root.apply {
            layoutManager =
                WearableLinearLayoutManager(this@MainActivity, HabiticaScrollingLayoutCallback())
            adapter = this@MainActivity.adapter
        }
    }



    override fun onStart() {
        super.onStart()
        binding.root.post {
            binding.root.setPaddingRelative(0, (binding.root.height * 0.25).toInt(), 0, (binding.root.height * 0.25).toInt())
            binding.root.scrollY = 0
        }
        adapter.data = listOf(
            MenuItem(
                "avatar",
                "Avatar",
                AppCompatResources.getDrawable(this, R.drawable.icon_rewards)
            ) {

            },
            MenuItem(
                "Stats",
                getString(R.string.stats),
                AppCompatResources.getDrawable(this, R.drawable.icon_rewards)
            ) {

            },
            MenuItem(
                "habits",
                getString(R.string.habits),
                AppCompatResources.getDrawable(this, R.drawable.icon_habits)
            ) {
              openTasklist(TaskType.HABIT)
            },
            MenuItem(
                "dailies",
                getString(R.string.dailies),
                AppCompatResources.getDrawable(this, R.drawable.icon_dailies)
            ) {
                openTasklist(TaskType.DAILY)
            },
            MenuItem(
                "todos",
                getString(R.string.todos),
                AppCompatResources.getDrawable(this, R.drawable.icon_todos)
            ) {
                openTasklist(TaskType.TODO)
            },
            MenuItem(
                "rewards",
                getString(R.string.rewards),
                AppCompatResources.getDrawable(this, R.drawable.icon_rewards)
            ) {
                openTasklist(TaskType.REWARD)
            }
        )
        viewModel.user.observe(this) {
            Log.d("MainActivity", "onStart: ${it.currentPet}")
        }
    }

    private fun openTasklist(type: TaskType) {
        val intent = Intent(this, TaskListActivity::class.java).apply {
            putExtra("type", type.name)
        }
        startActivity(intent)
    }
}

private const val MAX_ICON_PROGRESS = 0.8f

class HabiticaScrollingLayoutCallback : WearableLinearLayoutManager.LayoutCallback() {

    private var progressToCenter: Float = 0f

    override fun onLayoutFinished(child: View, parent: RecyclerView) {
        child.apply {
            // Figure out % progress from top to bottom
            val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
            val yRelativeToCenterOffset = y / parent.height + centerOffset

            // Normalize for center
            progressToCenter = abs(0.5f - yRelativeToCenterOffset) - 0.25f
            if (progressToCenter < 0) {
                scaleX = 1f
                scaleY = 1f
                alpha = 1f
                return
            }
            // Adjust to the maximum scale
            progressToCenter = Math.min(progressToCenter * 1.5f, MAX_ICON_PROGRESS)

            scaleX = 1 - progressToCenter
            scaleY = 1 - progressToCenter
            alpha = 1 - progressToCenter * 2
        }
    }
}