package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.TaskDescriptionBuilder
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.text.DateFormat
import javax.inject.Inject

class TaskSummaryViewModel(val taskId: String) : BaseViewModel() {
    @Inject
    lateinit var taskRespository: TaskRepository
    val task = taskRespository.getTask(taskId).asLiveData()

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val taskID: String): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskSummaryViewModel(taskID) as T
        }
    }
}

class TaskSummaryActivity: BaseActivity() {
    override fun getLayoutResId(): Int? = null

    private val viewModel: TaskSummaryViewModel by viewModels { TaskSummaryViewModel.Factory(intent.extras?.getString(
        TaskFormActivity.TASK_ID_KEY
    ) ?: "") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskSummaryView(viewModel = viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, viewModel.task.value?.lightTaskColor ?: R.color.brand_300)
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }
}

@Composable
fun TaskSummaryView(viewModel: TaskSummaryViewModel) {
    val taskDescriptionBuilder = TaskDescriptionBuilder(LocalContext.current)
    val task by viewModel.task.observeAsState()
    val titleModifier = Modifier.padding(top = 30.dp)
    val textModifier = Modifier.padding(top = 4.dp)
    val completedTimeFormat = DateFormat.getTimeInstance()
    val darkestColor = colorResource(task?.darkestTaskColor ?: R.color.text_primary)
    Column(Modifier.background(colorResource(task?.lightTaskColor ?: R.color.brand_300))) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 4.dp)) {
            Button(onClick = {
                MainNavigationController.navigateBack()
            }, colors =  ButtonDefaults.textButtonColors(contentColor = darkestColor), elevation = ButtonDefaults.elevation(0.dp)) {
                Image(painterResource(R.drawable.ic_arrow_back_white_36dp), stringResource(R.string.action_back), colorFilter = ColorFilter.tint(colorResource(task?.darkestTaskColor ?: R.color.white)))
            }
            Text(
                stringResource(R.string.task_summary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = darkestColor,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Column(
            Modifier
                .background(
                    MaterialTheme.colors.background,
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(20.dp, 5.dp)
                .fillMaxWidth()) {
            Text(stringResource(R.string.title), fontSize = 16.sp,  color = darkestColor, fontWeight = FontWeight.Medium, modifier = titleModifier)
            Text(task?.text ?: "", fontSize = 16.sp, color = MaterialTheme.colors.onBackground, modifier = textModifier)
            if (task?.notes?.isNotBlank() == true) {
                Text(
                    stringResource(R.string.notes),
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = titleModifier
                )
                Text(
                    task?.notes ?: "",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground,
                    modifier = textModifier
                )
            }
            if (task?.type != TaskType.REWARD) {
                Text(
                    stringResource(R.string.description),
                    fontSize = 16.sp,
                    color = darkestColor,
                    fontWeight = FontWeight.Medium,
                    modifier = titleModifier
                )
                Text(MarkdownParser.parseMarkdown(task?.let { taskDescriptionBuilder.describe(it) } ?: "").toString(),
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground,
                    modifier = textModifier)
            }
            if (task?.checklist?.isNotEmpty() == true) {
                task?.checklist?.let { checklist ->
                    Text(stringResource(R.string.checklist), fontSize = 16.sp, color = darkestColor, fontWeight = FontWeight.Medium, modifier = titleModifier)
                    for (item in checklist) {
                        Text(item.text ?: "", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier
                            .background(colorResource(R.color.gray_700))
                            .padding(15.dp)
                            .fillMaxWidth())
                    }
                }
            }
            if (task?.group?.assignedUsersDetail?.isNotEmpty() == true) {
                Text(stringResource(R.string.assigned_to), fontSize = 16.sp, color = darkestColor, fontWeight = FontWeight.Medium, modifier = titleModifier.padding(bottom = 4.dp))
                for (item in task?.group?.assignedUsersDetail ?: emptyList()) {
                    UserRow(item.assignedUsername ?: "", Modifier
                        .padding(vertical = 4.dp)
                        .background(colorResource(R.color.gray_700), RoundedCornerShape(8.dp))
                        .padding(15.dp, 12.dp)
                        .heightIn(min = 24.dp)
                        .fillMaxWidth(),
                    extraContent = if (item.completed) ({
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Image(painterResource(R.drawable.completed), null)
                            Text(stringResource(R.string.completed_at,
                                item.completedDate?.let { completedTimeFormat.format(it) } ?: ""),
                                fontSize = 14.sp,
                                color = colorResource(R.color.green_10), modifier = Modifier.padding(start = 4.dp))
                        }
                        }) else null)
                }
            }
        }
    }
}

@Composable
fun UserRow(username: String, modifier: Modifier = Modifier, extraContent: @Composable (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Column {
            Text(
                "@$username", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier
                    .background(colorResource(R.color.gray_700))
                    .fillMaxWidth()
            )
            if (extraContent != null) {
                extraContent()
            }
        }
    }
}

@Preview
@Composable
private fun TaskSummaryViewPreview() {
    TaskSummaryView(TaskSummaryViewModel(""))
}