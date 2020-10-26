package com.habitrpg.android.habitica.ui.fragments.skills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class SkillTasksRecyclerViewFragment : BaseFragment<FragmentRecyclerviewBinding>() {
    @Inject
    lateinit var taskRepository: TaskRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    var taskType: String? = null

    override var binding: FragmentRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecyclerviewBinding {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false)
    }

    var adapter: SkillTasksRecyclerViewAdapter = SkillTasksRecyclerViewAdapter(null, true)
    internal var layoutManager: LinearLayoutManager? = null

    private val taskSelectionEvents = PublishSubject.create<Task>()

    fun getTaskSelectionEvents(): Flowable<Task> {
        return taskSelectionEvents.toFlowable(BackpressureStrategy.DROP)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = SkillTasksRecyclerViewAdapter(null, true)
        compositeSubscription.add(adapter.getTaskSelectionEvents().subscribe({
            taskSelectionEvents.onNext(it)
        }, RxErrorHandler.handleEmptyError()))
        binding?.recyclerView?.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        var tasks = taskRepository.getTasks(taskType ?: "", userId)
        if (taskType == TaskType.TYPE_TODO) {
            tasks = tasks.map { it.where().equalTo("completed", false).findAll() }
        }
        compositeSubscription.add(tasks.subscribe({
            adapter.updateData(it)
        }, RxErrorHandler.handleEmptyError()))
    }
}
