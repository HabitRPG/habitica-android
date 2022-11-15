package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.asLiveData
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import javax.inject.Inject

class TaskSummaryViewModel(val taskId: String) : BaseViewModel() {
    @Inject
    lateinit var taskRespository: TaskRepository
    val task = taskRespository.getTask(taskId).asLiveData()

    override fun inject(component: UserComponent) {
        component.inject(this)
    }
}

class TaskSummaryActivity: BaseActivity() {
    override fun getLayoutResId(): Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }
}

@Composable
fun TaskSummaryView(viewModel: TaskSummaryViewModel) {
    val task by viewModel.task.observeAsState()
    Column {
        Text(stringResource(R.string.task_summary), Modifier)
    }
}

@Preview
@Composable
private fun TaskSummaryViewPreview() {
    TaskSummaryView(TaskSummaryViewModel(""))
}