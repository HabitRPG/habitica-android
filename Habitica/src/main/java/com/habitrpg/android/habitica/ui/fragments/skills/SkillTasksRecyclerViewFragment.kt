package com.habitrpg.android.habitica.ui.fragments.skills

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator

import javax.inject.Inject
import javax.inject.Named

import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.Flowable
import io.reactivex.functions.Consumer

class SkillTasksRecyclerViewFragment : BaseFragment() {
    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val recyclerView: RecyclerView? by bindView(R.id.recyclerView)

    var adapter: SkillTasksRecyclerViewAdapter = SkillTasksRecyclerViewAdapter(null, true)
    internal var layoutManager: LinearLayoutManager? = null
    var taskType: String? = null

    val taskSelectionEvents: Flowable<Task>
        get() = adapter.getTaskSelectionEvents()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_recyclerview)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compositeSubscription.add(taskRepository.getTasks(taskType ?: "", userId).firstElement().subscribe(Consumer { tasks -> adapter.updateData(tasks) }, RxErrorHandler.handleEmptyError()))
        recyclerView?.adapter = adapter

        layoutManager = recyclerView?.layoutManager as? LinearLayoutManager

        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(context)

            recyclerView?.layoutManager = layoutManager
        }
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()
    }
}
