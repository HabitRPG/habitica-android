package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.skills.SkillTasksRecyclerViewFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_prefs.*
import javax.inject.Inject
import javax.inject.Named

class SkillTasksActivity : BaseActivity() {

    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val viewPager: ViewPager by bindView(R.id.viewPager)
    private val tabLayout: TabLayout by bindView(R.id.tab_layout)

    internal var viewFragmentsDictionary = SparseArray<SkillTasksRecyclerViewFragment>()

    override fun getLayoutResId(): Int {
        return R.layout.activity_skill_tasks
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(toolbar)
        loadTaskLists()
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private fun loadTaskLists() {
        val fragmentManager = supportFragmentManager

        viewPager.adapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment {
                val fragment = SkillTasksRecyclerViewFragment()
                fragment.taskType = when (position) {
                    0 -> Task.TYPE_HABIT
                    1 -> Task.TYPE_DAILY
                    else -> Task.TYPE_TODO
                }

                compositeSubscription.add(fragment.getTaskSelectionEvents().subscribe(Consumer { task -> taskSelected(task) }, RxErrorHandler.handleEmptyError()))

                viewFragmentsDictionary.put(position, fragment)

                return fragment
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> getString(R.string.habits)
                    1 -> getString(R.string.dailies)
                    2 -> getString(R.string.todos)
                    else -> ""
                }
            }
        }


        tabLayout.setupWithViewPager(viewPager)
    }

    fun taskSelected(task: Task) {
        val resultIntent = Intent()
        resultIntent.putExtra("taskID", task.id)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

