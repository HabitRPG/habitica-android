package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.ActivitySkillTasksBinding
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.skills.SkillTasksRecyclerViewFragment
import com.habitrpg.shared.habitica.models.tasks.TaskType
import javax.inject.Inject
import javax.inject.Named

class SkillTasksActivity : BaseActivity() {
    private lateinit var binding: ActivitySkillTasksBinding

    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    internal var viewFragmentsDictionary = SparseArray<SkillTasksRecyclerViewFragment>()

    override fun getLayoutResId(): Int {
        return R.layout.activity_skill_tasks
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(findViewById(R.id.toolbar))
        loadTaskLists()
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivitySkillTasksBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private fun loadTaskLists() {
        val statePagerAdapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {

            override fun createFragment(position: Int): Fragment {
                val fragment = SkillTasksRecyclerViewFragment()
                fragment.taskType = when (position) {
                    0 -> TaskType.HABIT
                    1 -> TaskType.DAILY
                    else -> TaskType.TODO
                }
                fragment.onTaskSelection = {
                    taskSelected(it)
                }
                viewFragmentsDictionary.put(position, fragment)
                return fragment
            }

            override fun getItemCount(): Int {
                return 3
            }
        }
        binding.viewPager.adapter = statePagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.habits)
                1 -> getString(R.string.dailies)
                2 -> getString(R.string.todos)
                else -> ""
            }
        }.attach()
        statePagerAdapter.notifyDataSetChanged()
    }

    fun taskSelected(task: Task) {
        val resultIntent = Intent()
        resultIntent.putExtra("taskID", task.id)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }
}
