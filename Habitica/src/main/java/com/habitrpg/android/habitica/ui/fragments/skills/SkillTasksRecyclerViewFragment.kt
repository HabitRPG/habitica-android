package com.habitrpg.android.habitica.ui.fragments.skills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class SkillTasksRecyclerViewFragment : BaseFragment<FragmentRecyclerviewBinding>() {
    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var userViewModel: MainUserViewModel
    var taskType: TaskType? = null

    override var binding: FragmentRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecyclerviewBinding {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false)
    }

    var adapter: SkillTasksRecyclerViewAdapter = SkillTasksRecyclerViewAdapter()
    internal var layoutManager: LinearLayoutManager? = null

    var onTaskSelection: ((Task) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = SkillTasksRecyclerViewAdapter()
        adapter.onTaskSelection = { onTaskSelection?.invoke(it) }
        binding?.recyclerView?.adapter = adapter

        val additionalGroupIDs = userViewModel.mirrorGroupTasks.toTypedArray()
        var tasks = taskRepository.getTasks(taskType ?: TaskType.HABIT, userViewModel.userID, additionalGroupIDs)
            .map { it.filter { it.challengeID == null && it.group?.groupID?.isNotBlank() != true } }
        if (taskType == TaskType.TODO) {
            tasks = tasks.map { it.filter { !it.completed } }
        }
        tasks.asLiveData().observe(viewLifecycleOwner) {
            adapter.data = it
        }
    }
}
